package org.entando.kubernetes.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.entando.kubernetes.TestEntitiesGenerator.getTestBundle;

import org.entando.kubernetes.service.digitalexchange.BundleUtilities;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
public class EntandoBundleUtilitiesTest {

    @Test
    public void shouldReturnVersionDirectly() {
        String version = BundleUtilities.getBundleVersionOrFail(getTestBundle(), "1.0.0");
        assertThat(version).isEqualTo("1.0.0");
    }

    @Test
    public void shouldThrowAnErrorAsVersionIsNotDefined() {
        Exception ex = Assertions.assertThrows(RuntimeException.class, () -> {
            BundleUtilities.getBundleVersionOrFail(getTestBundle(), "first");
        });

        assertThat(ex.getMessage()).isEqualTo("Invalid version 'first' for bundle 'my-bundle'");
    }

    @Test
    public void shouldReturnLatestVersion() {
        String version = BundleUtilities.getBundleVersionOrFail(getTestBundle(), "latest");
        assertThat(version).isEqualTo("0.0.1");
    }

    @Test
    public void shouldVerifySemVersion() {
        assertThat(BundleUtilities.isSemanticVersion("v0.0.1")).isTrue();
        assertThat(BundleUtilities.isSemanticVersion("0.1.10-SNAPSHOT")).isTrue();
        assertThat(BundleUtilities.isSemanticVersion("my-great-version")).isFalse();
    }


}
