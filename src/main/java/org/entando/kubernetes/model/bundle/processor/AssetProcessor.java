package org.entando.kubernetes.model.bundle.processor;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.entando.kubernetes.client.core.EntandoCoreClient;
import org.entando.kubernetes.exception.EntandoComponentManagerException;
import org.entando.kubernetes.model.bundle.BundleProperty;
import org.entando.kubernetes.model.bundle.BundleReader;
import org.entando.kubernetes.model.bundle.descriptor.FileDescriptor;
import org.entando.kubernetes.model.bundle.installable.AssetInstallable;
import org.entando.kubernetes.model.bundle.installable.DirectoryInstallable;
import org.entando.kubernetes.model.bundle.installable.Installable;
import org.entando.kubernetes.model.digitalexchange.ComponentType;
import org.entando.kubernetes.model.digitalexchange.EntandoBundleComponentJob;
import org.entando.kubernetes.model.digitalexchange.EntandoBundleJob;
import org.springframework.stereotype.Service;

/**
 * Processor to handle Static files to be stored by Entando. Commonly used for js, images and css.
 * <p>
 * This processor will also create the folders.
 *
 * @author Sergio Marcelino
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AssetProcessor implements ComponentProcessor {

    private final EntandoCoreClient engineService;

    @Override
    public ComponentType getComponentType() {
        return ComponentType.ASSET;
    }

    @Override
    public List<Installable> process(EntandoBundleJob job, BundleReader npr) {
        final List<Installable> installables = new LinkedList<>();

        try {
            if (npr.containsResourceFolder()) {
                final String componentFolder = "/" + npr.getBundleCode();

                List<String> resourceFiles = npr.getResourceFiles().stream().sorted().collect(Collectors.toList());
                for (final String resourceFile : resourceFiles) {
                    final FileDescriptor fileDescriptor = npr.getResourceFileAsDescriptor(resourceFile);

                    Path fileFolder = Paths.get(BundleProperty.RESOURCES_FOLDER_PATH.getValue())
                            .relativize(Paths.get(fileDescriptor.getFolder()));
                    String folder = Paths.get(componentFolder).resolve(fileFolder).toString();
                    fileDescriptor.setFolder(folder);
                    installables.add(new AssetInstallable(engineService, fileDescriptor));
                }
            }
        } catch (IOException e) {
            throw new EntandoComponentManagerException("Error reading bundle", e);
        }

        return installables;
    }

    @Override
    public List<Installable> process(List<EntandoBundleComponentJob> components) {
        return components.stream()
                .filter(c -> c.getComponentType() == ComponentType.ASSET)
                .map(c -> new AssetInstallable(engineService, c))
                .collect(Collectors.toList());
    }
}
