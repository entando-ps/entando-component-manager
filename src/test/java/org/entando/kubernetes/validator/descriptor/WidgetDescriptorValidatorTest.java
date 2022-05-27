package org.entando.kubernetes.validator.descriptor;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import java.io.File;
import java.io.IOException;
import org.entando.kubernetes.exception.digitalexchange.InvalidBundleException;
import org.entando.kubernetes.model.bundle.descriptor.widget.WidgetDescriptor;
import org.entando.kubernetes.model.bundle.descriptor.widget.WidgetDescriptorVersion;
import org.entando.kubernetes.stubhelper.WidgetStubHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class WidgetDescriptorValidatorTest {

    private WidgetDescriptorValidator validator;
    private final YAMLMapper yamlMapper = new YAMLMapper();

    @BeforeEach
    public void genericSetup() {
        validator = new WidgetDescriptorValidator();
        validator.setupValidatorConfiguration();
    }

    @Test
    void shouldCorrectlyValidateWidgetDescriptorV1() throws IOException {
        WidgetDescriptor descriptor = yamlMapper
                .readValue(new File("src/test/resources/bundle/widgets/my_widget_descriptor.yaml"),
                        WidgetDescriptor.class);
        assertDoesNotThrow(() -> validator.validateOrThrow(descriptor));
    }

    @Test
    void shouldThrowExceptionWhileValidatingAnInvalidWidgetDescriptorV1() throws IOException {
        WidgetDescriptor descriptor = yamlMapper
                .readValue(new File("src/test/resources/bundle/widgets/my_widget_descriptor_v2.yaml"),
                        WidgetDescriptor.class);
        descriptor.setDescriptorVersion(WidgetDescriptorVersion.V1.getVersion());
        assertThrows(InvalidBundleException.class, () -> validator.validateOrThrow(descriptor));
    }

    @Test
    void shouldCorrectlyValidateWidgetDescriptorV2() throws IOException {
        WidgetDescriptor descriptor = yamlMapper
                .readValue(new File("src/test/resources/bundle/widgets/my_widget_descriptor_v2.yaml"),
                        WidgetDescriptor.class);
        assertDoesNotThrow(() -> validator.validateOrThrow(descriptor));
    }

    @Test
    void shouldThrowExceptionWhileValidatingAWidgetDescriptorV2ContainingUnexpectedFields() throws IOException {
        WidgetDescriptor descriptor = yamlMapper
                .readValue(new File("src/test/resources/bundle/widgets/my_widget_descriptor.yaml"),
                        WidgetDescriptor.class);
        descriptor.setDescriptorVersion(WidgetDescriptorVersion.V2.getVersion());
        assertThrows(InvalidBundleException.class, () -> validator.validateOrThrow(descriptor));
    }

    @Test
    void shouldThrowExceptionWhileValidatingAWidgetDescriptorWithInvalidApiClaims() {
        // internal api with bundle id
        WidgetDescriptor descriptor = WidgetStubHelper.stubWidgetDescriptorV2();
        descriptor.getApiClaims().get(0).setBundleId("id");
        assertThrows(InvalidBundleException.class, () -> validator.validateOrThrow(descriptor));

        // external api without bundle id
        descriptor.getApiClaims().get(0).setBundleId(null);
        descriptor.getApiClaims().get(1).setBundleId(null);
        assertThrows(InvalidBundleException.class, () -> validator.validateOrThrow(descriptor));
    }
}
