package org.entando.kubernetes.service.digitalexchange.templating;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;
import org.entando.kubernetes.exception.EntandoComponentManagerException;
import org.entando.kubernetes.model.bundle.descriptor.widget.WidgetDescriptor;
import org.entando.kubernetes.model.bundle.descriptor.widget.WidgetDescriptor.ApiClaim;
import org.entando.kubernetes.model.bundle.reader.BundleReader;
import org.entando.kubernetes.model.job.PluginAPIDataEntity;
import org.entando.kubernetes.repository.PluginAPIDataRepository;
import org.entando.kubernetes.stubhelper.WidgetStubHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class WidgetTemplateGeneratorServiceImplTest {

    @Mock
    private BundleReader bundleReader;
    @Mock
    private PluginAPIDataRepository repository;

    private WidgetDescriptor descriptor = WidgetStubHelper.stubWidgetDescriptorV2();
    private WidgetTemplateGeneratorServiceImpl service;

    private PluginAPIDataEntity extApiDataEntity = new PluginAPIDataEntity()
            .setIngressPath(WidgetStubHelper.PLUGIN_INGRESS_2_PATH);

    @BeforeEach
    public void setup() {
        service = new WidgetTemplateGeneratorServiceImpl(repository);
    }

    @Test
    void shouldCreateTheExpectedResourceTags() {

        when(bundleReader.getWidgetResourcesOfType(descriptor.getCode(), "js")).thenReturn(
                WidgetStubHelper.JS_RESOURCES);
        when(bundleReader.getWidgetResourcesOfType(descriptor.getCode(), "css")).thenReturn(
                WidgetStubHelper.CSS_RESOURCES);

        String expected = "<script src=\"<@wp.resourceURL />widgets/my-code/static/js/main.js\"></script>\n"
                + "<script src=\"<@wp.resourceURL />widgets/my-code/static/js/runtime.js\"></script>\n\n"
                + "<link href=\"<@wp.resourceURL />widgets/my-code/static/css/style.css\" rel=\"stylesheet\">";

        final String resourceTags = service.createResourceTags(descriptor.getCode(), bundleReader);
        assertThat(resourceTags).isEqualTo(expected);
    }

    @Test
    void shouldCreateTheExpectedAssignTags() {
        when(repository.findByBundleIdAndServiceId(WidgetStubHelper.API_CLAIM_2_BUNDLE_ID,
                WidgetStubHelper.API_CLAIM_2_SERVICE_ID)).thenReturn(Optional.of(extApiDataEntity));

        final String tag = service.createAssignTag(descriptor);
        assertThat(tag).isEqualTo(
                "<#assign mfeSystemConfig=\"{\"systemParams\": \"{\"api\": {\"int-api\":{\"url\":\"service-id-1/path\"}, "
                        + "\"ext-api\":{\"url\":\"service-id-2/path\"}}}}\">");
    }

    @Test
    void shouldThrowExceptionIfCantProvideApiPathWhileCreatingTheAssignTags() {
        WidgetDescriptor descriptor = WidgetStubHelper.stubWidgetDescriptorV2();
        descriptor.getApiClaims().get(1).setServiceId("non-existing");
        assertThrows(EntandoComponentManagerException.class, () -> service.createAssignTag(descriptor));
    }

    @Test
    void shouldCreateTheExpectedCustomElementTag() {
        String tag = service.createCustomElementTag(descriptor);
        assertThat(tag).isEqualTo("<x-" + WidgetStubHelper.WIDGET_1_CODE + "-widget config=\"${mfeConfig}\"/>");
    }

    @Test
    void shouldCreateTheExpectedApiAttribute() {
        // from the current bundle, so get the path from the descriptor metadata
        String tag = service.createApiAttribute(descriptor.getApiClaims().get(0),
                descriptor.getDescriptorMetadata().getPluginIngressPathMap());
        assertThat(tag).isEqualTo("\"int-api\":{\"url\":\"service-id-1/path\"}");

        // from a previously installed bundle, so get the path from the db
        when(repository.findByBundleIdAndServiceId(WidgetStubHelper.API_CLAIM_2_BUNDLE_ID,
                WidgetStubHelper.API_CLAIM_2_SERVICE_ID)).thenReturn(Optional.of(extApiDataEntity));

        tag = service.createApiAttribute(descriptor.getApiClaims().get(1),
                descriptor.getDescriptorMetadata().getPluginIngressPathMap());
        assertThat(tag).isEqualTo("\"ext-api\":{\"url\":\"service-id-2/path\"}");
    }

    @Test
    void shouldThrowExceptionWhileCreatingApiAttributeForNonExistingPaths() {
        WidgetDescriptor descriptor = WidgetStubHelper.stubWidgetDescriptorV2();
        ApiClaim apiClaim = descriptor.getApiClaims().get(0);
        apiClaim.setServiceId("non-existing");
        final Map<String, String> pluginIngressPathMap = descriptor.getDescriptorMetadata().getPluginIngressPathMap();

        assertThrows(EntandoComponentManagerException.class,
                () -> service.createApiAttribute(apiClaim, pluginIngressPathMap));
    }

    @Test
    void shouldGenerateTheExpectedWidgetTemplate() {

        when(bundleReader.getWidgetResourcesOfType(descriptor.getCode(), "js")).thenReturn(
                WidgetStubHelper.JS_RESOURCES);
        when(bundleReader.getWidgetResourcesOfType(descriptor.getCode(), "css")).thenReturn(
                WidgetStubHelper.CSS_RESOURCES);

        when(repository.findByBundleIdAndServiceId(WidgetStubHelper.API_CLAIM_2_BUNDLE_ID,
                WidgetStubHelper.API_CLAIM_2_SERVICE_ID)).thenReturn(Optional.of(extApiDataEntity));

        String expected = "<#assign wp=JspTaglibs[\"/aps-core\"]>\n\n"
                + "<@wp.info key=\"systemParam\" paramName=\"applicationBaseURL\" var=\"systemParam_applicationBaseURL\" />\n\n"
                + "<script src=\"<@wp.resourceURL />widgets/my-code/static/js/main.js\"></script>\n"
                + "<script src=\"<@wp.resourceURL />widgets/my-code/static/js/runtime.js\"></script>\n\n"
                + "<link href=\"<@wp.resourceURL />widgets/my-code/static/css/style.css\" rel=\"stylesheet\">\n\n"
                + "<#assign mfeSystemConfig=\"{\"systemParams\": \"{\"api\": {\"int-api\":{\"url\":\"service-id-1/path\"}, \"ext-api\":{\"url\":\"service-id-2/path\"}}}}\">\n\n"
                + "<x-" + WidgetStubHelper.WIDGET_1_CODE + "-widget config=\"${mfeConfig}\"/>";

        final String ftl = service.generateWidgetTemplate(descriptor, bundleReader);

        assertThat(ftl).isEqualTo(expected);
    }
}
