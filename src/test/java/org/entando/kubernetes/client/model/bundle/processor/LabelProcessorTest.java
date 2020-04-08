package org.entando.kubernetes.client.model.bundle.processor;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.entando.kubernetes.model.bundle.BundleReader;
import org.entando.kubernetes.model.bundle.descriptor.ComponentDescriptor;
import org.entando.kubernetes.model.bundle.descriptor.ComponentSpecDescriptor;
import org.entando.kubernetes.model.bundle.descriptor.LabelDescriptor;
import org.entando.kubernetes.model.bundle.installable.Installable;
import org.entando.kubernetes.model.bundle.processor.LabelProcessor;
import org.entando.kubernetes.model.bundle.installable.LabelInstallable;
import org.entando.kubernetes.model.digitalexchange.ComponentType;
import org.entando.kubernetes.model.digitalexchange.DigitalExchangeJob;
import org.entando.kubernetes.service.digitalexchange.entandocore.EntandoCoreService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@Tag("unit")
public class LabelProcessorTest {

    @Mock private EntandoCoreService engineService;
    @Mock private BundleReader bundleReader;

    private LabelProcessor processor;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        processor = new LabelProcessor(engineService);
    }

    @Test
    public void testCreateLabels() throws IOException, ExecutionException, InterruptedException {
        final DigitalExchangeJob job = new DigitalExchangeJob();
        job.setComponentId("my-component-id");

        final ComponentSpecDescriptor spec = new ComponentSpecDescriptor();
        final ComponentDescriptor descriptor = new ComponentDescriptor("my-component", "desc", spec);
        spec.setLabels(singletonList(new LabelDescriptor("HELLO", singletonMap("en", "Hello"))));

        final List<? extends Installable> installables = processor.process(job, bundleReader, descriptor);

        assertThat(installables).hasSize(1);
        assertThat(installables.get(0)).isInstanceOf(LabelInstallable.class);
        assertThat(installables.get(0).getComponentType()).isEqualTo(ComponentType.LABEL);
        assertThat(installables.get(0).getName()).isEqualTo("HELLO");

        verify(engineService, times(0)).registerLabel(any());
        installables.get(0).install().get();

        final ArgumentCaptor<LabelDescriptor> captor = ArgumentCaptor.forClass(LabelDescriptor.class);
        verify(engineService, times(1)).registerLabel(captor.capture());
        final LabelDescriptor labelDescriptor = captor.getValue();

        assertThat(labelDescriptor.getKey()).isEqualTo("HELLO");
        assertThat(labelDescriptor.getTitles()).hasSize(1);
        assertThat(labelDescriptor.getTitles().get("en")).isEqualTo("Hello");
    }

}
