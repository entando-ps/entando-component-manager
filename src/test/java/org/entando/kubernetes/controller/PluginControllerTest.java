package org.entando.kubernetes.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import java.util.List;
import org.entando.kubernetes.model.plugin.EntandoPlugin;
import org.entando.kubernetes.model.plugin.EntandoPluginBuilder;
import org.entando.kubernetes.service.KubernetesService;
import org.entando.web.exception.NotFoundException;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.client.HttpClientErrorException;

@SpringBootTest
@AutoConfigureMockMvc
@RunWith(SpringRunner.class)
public class PluginControllerTest {

    private static final String URL = "/plugins";

    @Autowired private MockMvc mockMvc;

    @MockBean
    private KubernetesService kubernetesService;


    @Test
    public void testListEmpty() throws Exception {
        when(kubernetesService.getLinkedPlugins()).thenReturn(Collections.emptyList());

        mockMvc.perform(get(URL))
                .andDo(print()).andExpect(status().isOk())
                .andExpect(jsonPath("payload", hasSize(0)));
    }

    @Test
    @Ignore("NotFound status has not be ported. Evaluate to use Zalando error handling framework")
    public void testNotFound() throws Exception {
        String pluginId = "arbitrary-plugin";

        mockMvc.perform(get(String.format("%s/%s", URL, pluginId)))
                .andDo(print()).andExpect(status().isNotFound());

    }

    @Test
    public void testList() throws Exception {
        List<EntandoPlugin> linkedPlugins = Collections.singletonList(getTestEntandoPlugin());
        when(kubernetesService.getLinkedPlugins()).thenReturn(linkedPlugins);

        mockMvc.perform(get(URL))
                .andDo(print()).andExpect(status().isOk())
                .andExpect(jsonPath("payload", hasSize(1)));

    }

    @Test
    public void testSinglePlugin() throws Exception {
        when(kubernetesService.getLinkedPlugin(anyString())).thenReturn(getTestEntandoPlugin());

        mockMvc.perform(get(URL))
                .andDo(print()).andExpect(status().isOk());

    }

    private EntandoPlugin getTestEntandoPlugin() {
        return new EntandoPluginBuilder()
                    .withNewSpec()
                    .withReplicas(1)
                    .withIngressPath("/pluginpath")
                    .endSpec()
                    .withNewMetadata()
                    .withName("plugin-name")
                    .endMetadata()
                    .build();
    }

    private void validate(final ResultActions actions, final String prefix) throws Exception {
        actions.andExpect(jsonPath(prefix + "plugin").value("plugin-name"))
                .andExpect(jsonPath(prefix + "online").value(true))
                .andExpect(jsonPath(prefix + "path").value("/pluginpath"))
                .andExpect(jsonPath(prefix + "replicas").value(1))
                .andExpect(jsonPath(prefix + "deploymentPhase").value("successful"))
                .andExpect(jsonPath(prefix + "serverStatus.type").value("jeeServer"))
                .andExpect(jsonPath(prefix + "serverStatus.replicas").value(1))
                .andExpect(jsonPath(prefix + "serverStatus.volumePhase").value("Bound"))
                .andExpect(jsonPath(prefix + "serverStatus.podStatus.phase").value("Running"))
                .andExpect(jsonPath(prefix + "serverStatus.podStatus.conditions", hasSize(1)))
                .andExpect(jsonPath(prefix + "serverStatus.podStatus.conditions[0].lastTransitionTime").value("2019-07-11T18:36:06Z"))
                .andExpect(jsonPath(prefix + "serverStatus.podStatus.conditions[0].status").value("True"))
                .andExpect(jsonPath(prefix + "serverStatus.podStatus.conditions[0].type").value("Initialized"))
                .andExpect(jsonPath(prefix + "serverStatus.deploymentStatus.availableReplicas").value(1))
                .andExpect(jsonPath(prefix + "serverStatus.deploymentStatus.readyReplicas").value(1))
                .andExpect(jsonPath(prefix + "serverStatus.deploymentStatus.replicas").value(1))
                .andExpect(jsonPath(prefix + "serverStatus.deploymentStatus.updatedReplicas").value(1))
                .andExpect(jsonPath(prefix + "serverStatus.deploymentStatus.conditions[0].lastTransitionTime").value("2019-07-11T18:36:06Z"))
                .andExpect(jsonPath(prefix + "serverStatus.deploymentStatus.conditions[0].lastUpdateTime").value("2019-07-11T18:36:06Z"))
                .andExpect(jsonPath(prefix + "serverStatus.deploymentStatus.conditions[0].status").value("True"))
                .andExpect(jsonPath(prefix + "serverStatus.deploymentStatus.conditions[0].type").value("Progressing"))
                .andExpect(jsonPath(prefix + "serverStatus.deploymentStatus.conditions[0].reason").value("NewReplicaSetAvailable"))
                .andExpect(jsonPath(prefix + "serverStatus.deploymentStatus.conditions[0].message").value("Some message"))

                .andExpect(jsonPath(prefix + "externalServiceStatuses", hasSize(1)))
                .andExpect(jsonPath(prefix + "externalServiceStatuses[0].type").value("dbServer"))
                .andExpect(jsonPath(prefix + "externalServiceStatuses[0].replicas").value(1))
                .andExpect(jsonPath(prefix + "externalServiceStatuses[0].volumePhase").value("Bound"))
                .andExpect(jsonPath(prefix + "externalServiceStatuses[0].podStatus.phase").value("Running"))
                .andExpect(jsonPath(prefix + "externalServiceStatuses[0].podStatus.conditions", hasSize(2)))
                .andExpect(jsonPath(prefix + "externalServiceStatuses[0].podStatus.conditions[0].lastTransitionTime").value("2019-07-11T18:36:06Z"))
                .andExpect(jsonPath(prefix + "externalServiceStatuses[0].podStatus.conditions[0].status").value("True"))
                .andExpect(jsonPath(prefix + "externalServiceStatuses[0].podStatus.conditions[0].type").value("Initialized"))
                .andExpect(jsonPath(prefix + "externalServiceStatuses[0].podStatus.conditions[1].lastTransitionTime").value("2019-07-11T18:36:09Z"))
                .andExpect(jsonPath(prefix + "externalServiceStatuses[0].podStatus.conditions[1].status").value("True"))
                .andExpect(jsonPath(prefix + "externalServiceStatuses[0].podStatus.conditions[1].type").value("Available"))
                .andExpect(jsonPath(prefix + "externalServiceStatuses[0].deploymentStatus.availableReplicas").value(1))
                .andExpect(jsonPath(prefix + "externalServiceStatuses[0].deploymentStatus.readyReplicas").value(1))
                .andExpect(jsonPath(prefix + "externalServiceStatuses[0].deploymentStatus.replicas").value(1))
                .andExpect(jsonPath(prefix + "externalServiceStatuses[0].deploymentStatus.updatedReplicas").value(1))
                .andExpect(jsonPath(prefix + "externalServiceStatuses[0].deploymentStatus.conditions", hasSize(2)))
                .andExpect(jsonPath(prefix + "externalServiceStatuses[0].deploymentStatus.conditions[0].lastTransitionTime").value("2019-07-11T18:36:03Z"))
                .andExpect(jsonPath(prefix + "externalServiceStatuses[0].deploymentStatus.conditions[0].lastUpdateTime").value("2019-07-11T18:36:03Z"))
                .andExpect(jsonPath(prefix + "externalServiceStatuses[0].deploymentStatus.conditions[0].status").value("True"))
                .andExpect(jsonPath(prefix + "externalServiceStatuses[0].deploymentStatus.conditions[0].type").value("Progressing"))
                .andExpect(jsonPath(prefix + "externalServiceStatuses[0].deploymentStatus.conditions[0].reason").value("NewReplicaSetAvailable"))
                .andExpect(jsonPath(prefix + "externalServiceStatuses[0].deploymentStatus.conditions[0].message").value("Some message"))
                .andExpect(jsonPath(prefix + "externalServiceStatuses[0].deploymentStatus.conditions[1].lastTransitionTime").value("2019-07-11T18:36:06Z"))
                .andExpect(jsonPath(prefix + "externalServiceStatuses[0].deploymentStatus.conditions[1].lastUpdateTime").value("2019-07-11T18:36:06Z"))
                .andExpect(jsonPath(prefix + "externalServiceStatuses[0].deploymentStatus.conditions[1].status").value("True"))
                .andExpect(jsonPath(prefix + "externalServiceStatuses[0].deploymentStatus.conditions[1].type").value("Available"))
                .andExpect(jsonPath(prefix + "externalServiceStatuses[0].deploymentStatus.conditions[1].reason").value("MinimumReplicasAvailable"))
                .andExpect(jsonPath(prefix + "externalServiceStatuses[0].deploymentStatus.conditions[1].message").value("Some message"))
        ;
    }

}
