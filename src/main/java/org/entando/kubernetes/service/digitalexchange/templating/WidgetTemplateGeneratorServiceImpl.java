package org.entando.kubernetes.service.digitalexchange.templating;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.entando.kubernetes.exception.EntandoComponentManagerException;
import org.entando.kubernetes.model.bundle.descriptor.widget.WidgetDescriptor;
import org.entando.kubernetes.model.bundle.reader.BundleReader;
import org.entando.kubernetes.model.job.PluginAPIDataEntity;
import org.entando.kubernetes.repository.PluginAPIDataRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

@Service
@RequiredArgsConstructor
public class WidgetTemplateGeneratorServiceImpl implements WidgetTemplateGeneratorService {

    public static final String JS_TYPE = "js";
    public static final String CSS_TYPE = "css";
    public static final String APS_CORE_TAG = "<#assign wp=JspTaglibs[\"/aps-core\"]>\n\n";
    public static final String APP_BASE_URL_TAG = "<@wp.info key=\"systemParam\" paramName=\"applicationBaseURL\" var=\"systemParam_applicationBaseURL\" />\n\n";
    public static final String SCRIPT_TAG = "<script src=\"<@wp.resourceURL />%s\"></script>";
    public static final String CSS_TAG = "<link href=\"<@wp.resourceURL />%s\" rel=\"stylesheet\">";
    public static final String ASSIGN_TAG = "<#assign mfeSystemConfig=\"{\"systemParams\": \"{\"api\": {%s}}}\">";
    public static final String API_ATTRIBUTE = "\"%s\":{\"url\":\"%s\"}";
    public static final String CUSTOM_ELEMENT_TAG = "<x-%s-widget config=\"${mfeConfig}\"/>";

    private final PluginAPIDataRepository apiPathRepository;

    @Override
    public String generateWidgetTemplate(WidgetDescriptor descriptor, BundleReader bundleReader) {
        return APS_CORE_TAG
                + APP_BASE_URL_TAG
                + createResourceTags(descriptor.getCode(), bundleReader) + "\n"
                + "\n" + createAssignTag(descriptor) + "\n"
                + "\n" + createCustomElementTag(descriptor);
    }

    protected String createResourceTags(String widgetCode, BundleReader bundleReader) {

        String ftl = bundleReader.getWidgetResourcesOfType(widgetCode, JS_TYPE).stream()
                .map(file -> String.format(SCRIPT_TAG, file))
                .collect(Collectors.joining("\n"));

        ftl += "\n\n";

        ftl += bundleReader.getWidgetResourcesOfType(widgetCode, CSS_TYPE).stream()
                .map(file -> String.format(CSS_TAG, file))
                .collect(Collectors.joining("\n"));

        return ftl;
    }

    protected String createAssignTag(WidgetDescriptor descriptor) {
        final String apiAttributes = Optional.ofNullable(descriptor.getApiClaims()).orElseGet(ArrayList::new)
                .stream().map(apiClaim -> createApiAttribute(apiClaim,
                        descriptor.getDescriptorMetadata().getPluginIngressPathMap()))
                .collect(Collectors.joining(", "));

        return String.format(ASSIGN_TAG, apiAttributes);
    }

    protected String createCustomElementTag(WidgetDescriptor descriptor) {
        return String.format(CUSTOM_ELEMENT_TAG, descriptor.getCode());
    }

    protected String createApiAttribute(WidgetDescriptor.ApiClaim apiClaim, Map<String, String> pluginIngressPathMap) {

        String ingressPath;
        if (apiClaim.getType().equals(WidgetDescriptor.ApiClaim.INTERNAL_API)) {
            ingressPath = pluginIngressPathMap.get(apiClaim.getServiceId());
        } else {
            ingressPath = apiPathRepository.findByBundleIdAndServiceId(apiClaim.getBundleId(), apiClaim.getServiceId())
                    .map(PluginAPIDataEntity::getIngressPath)
                    .orElse(null);
        }

        if (ObjectUtils.isEmpty(ingressPath)) {
            throw new EntandoComponentManagerException("Can't supply the claimed API " + apiClaim.getName());
        }

        return String.format(API_ATTRIBUTE, apiClaim.getName(), ingressPath);
    }
}
