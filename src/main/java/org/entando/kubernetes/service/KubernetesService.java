package org.entando.kubernetes.service;

import static org.entando.kubernetes.model.EntandoDeploymentPhase.FAILED;
import static org.entando.kubernetes.model.EntandoDeploymentPhase.SUCCESSFUL;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.awaitility.core.ConditionFactory;
import org.awaitility.core.ConditionTimeoutException;
import org.entando.kubernetes.client.k8ssvc.K8SServiceClient;
import org.entando.kubernetes.exception.k8ssvc.EntandoAppPluginLinkingProcessException;
import org.entando.kubernetes.exception.k8ssvc.PluginNotFoundException;
import org.entando.kubernetes.exception.k8ssvc.PluginNotReadyException;
import org.entando.kubernetes.model.debundle.EntandoDeBundle;
import org.entando.kubernetes.model.link.EntandoAppPluginLink;
import org.entando.kubernetes.model.plugin.EntandoPlugin;
import org.entando.kubernetes.model.plugin.EntandoPluginBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Slf4j
@Component
public class KubernetesService {

    public static final String ENTANDOPLUGIN_CRD_NAME = "entandoplugins.entando.org";
    public static final String KUBERNETES_NAMESPACE_PATH = "/var/run/secrets/kubernetes.io/serviceaccount/namespace";

    private final ConditionFactory waitingConditionFactory;
    private final K8SServiceClient k8sServiceClient;
    private final String entandoAppName;
    private final String entandoAppNamespace;
    private Set<String> digitalExchangesNames;

    public KubernetesService(@Value("${entando.app.name}") String entandoAppName,
            @Value("${entando.app.namespace}") String entandoAppNamespace,
            @Value("${entando.component.repository.namespaces}") Set<String> digitalExchangesNames,
            K8SServiceClient k8SServiceClient,
            ConditionFactory waitingConditionFactory) {
        this.waitingConditionFactory = waitingConditionFactory;
        this.k8sServiceClient = k8SServiceClient;
        this.entandoAppName = entandoAppName;
        this.digitalExchangesNames = digitalExchangesNames;
        this.entandoAppNamespace = getCurrentKubernetesNamespace().orElse(entandoAppNamespace);
    }

    public List<EntandoPlugin> getLinkedPlugins() {
        return getCurrentAppLinks()
                .stream().map(k8sServiceClient::getPluginForLink)
                .collect(Collectors.toList());
    }

    public EntandoPlugin getLinkedPlugin(String pluginId) {
        return getCurrentAppLinkToPlugin(pluginId)
                .map(k8sServiceClient::getPluginForLink)
                .orElseThrow(PluginNotFoundException::new);
    }

    public boolean isLinkedPlugin(String pluginId) {
        return getCurrentAppLinkToPlugin(pluginId).isPresent();
    }

    public boolean isPluginReady(EntandoPlugin plugin) {
        return k8sServiceClient.isPluginReadyToServeApp(plugin, entandoAppName);
    }

    private List<EntandoAppPluginLink> getCurrentAppLinks() {
        return k8sServiceClient.getAppLinks(entandoAppName);
    }

    public Optional<EntandoAppPluginLink> getCurrentAppLinkToPlugin(String pluginId) {
        return getCurrentAppLinks()
                .stream()
                .filter(el -> el.getSpec().getEntandoPluginName().equals(pluginId))
                .findFirst();
    }

    public void unlink(String pluginId) {
        getCurrentAppLinkToPlugin(pluginId).ifPresent(k8sServiceClient::unlink);
    }

    public void unlinkAndScaleDownPlugin(String pluginId) {
        getCurrentAppLinkToPlugin(pluginId).ifPresent(k8sServiceClient::unlinkAndScaleDown);
    }


    public EntandoAppPluginLink linkPlugin(EntandoPlugin plugin) {
        EntandoPlugin newPlugin = createNewPlugin(plugin);
        return k8sServiceClient.linkAppWithPlugin(entandoAppName, entandoAppNamespace, newPlugin);
    }

    public void linkPluginAndWaitForSuccess(EntandoPlugin plugin) {
        EntandoAppPluginLink createdLink = this.linkPlugin(plugin);
        try {
            this.waitingConditionFactory.until(() -> this.hasLinkingProcessCompletedSuccessfully(createdLink, plugin));
        } catch (ConditionTimeoutException e) {
            throw new PluginNotReadyException(plugin.getMetadata().getName());
        }

    }

    private EntandoPlugin createNewPlugin(EntandoPlugin plugin) {
        EntandoPlugin newPlugin = new EntandoPluginBuilder()
                .withMetadata(plugin.getMetadata())
                .withSpec(plugin.getSpec())
                .build();

        newPlugin.getMetadata().setNamespace(this.entandoAppNamespace);

        return newPlugin;
    }

    public boolean hasLinkingProcessCompletedSuccessfully(EntandoAppPluginLink link, EntandoPlugin plugin) {
        boolean result = false;
        Optional<EntandoAppPluginLink> linkByName = k8sServiceClient.getLinkByName(link.getMetadata().getName());
        if (linkByName.isPresent()) {
            if (linkByName.get().getStatus().getEntandoDeploymentPhase().equals(FAILED)) {
                String msg = String.format("Linking procedure between app %s and plugin %s failed", entandoAppName,
                        plugin.getMetadata().getName());
                throw new EntandoAppPluginLinkingProcessException(msg);
            }
            result = linkByName.get().getStatus().getEntandoDeploymentPhase().equals(SUCCESSFUL)
                    && isPluginReady(plugin);
        }
        return result;
    }

    public List<EntandoDeBundle> getBundlesInDefaultNamespace() {
        return k8sServiceClient.getBundlesInObservedNamespaces();
    }

    public Optional<EntandoDeBundle> fetchBundleByName(String name) {
        if (CollectionUtils.isEmpty(this.digitalExchangesNames)) {
            log.info("Fetching bundle by name {}", name);
            return this.getBundleByName(name);
        }

        return this.digitalExchangesNames.stream()
                .map(namespace -> {
                    log.info("Fetching bundle by name {} in namespace {}", name, namespace);
                    return this.getBundleByNameAndNamespace(name, namespace).orElse(null);
                })
                .filter(Objects::nonNull)
                .findFirst();
    }

    protected Optional<EntandoDeBundle> getBundleByName(String name) {
        return k8sServiceClient.getBundleWithName(name);
    }

    protected Optional<EntandoDeBundle> getBundleByNameAndNamespace(String name, String namespace) {
        return k8sServiceClient.getBundleWithNameAndNamespace(name, namespace);
    }

    private Optional<String> getCurrentKubernetesNamespace() {
        Path namespacePath = Paths.get(KUBERNETES_NAMESPACE_PATH);
        String namespace = null;
        if (namespacePath.toFile().exists()) {
            try {
                namespace = new String(Files.readAllBytes(namespacePath));
            } catch (IOException e) {
                log.error(String.format("An error occurred while reading the namespace from file %s",
                        namespacePath.toString()), e);
            }
        }
        return Optional.ofNullable(namespace);
    }

}
