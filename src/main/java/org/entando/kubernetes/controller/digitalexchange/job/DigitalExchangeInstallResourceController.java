package org.entando.kubernetes.controller.digitalexchange.job;

import javax.servlet.http.HttpServletRequest;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.entando.kubernetes.exception.job.JobNotFoundException;
import org.entando.kubernetes.exception.k8ssvc.BundleNotFoundException;
import org.entando.kubernetes.exception.k8ssvc.K8SServiceClientException;
import org.entando.kubernetes.model.digitalexchange.DigitalExchangeJob;
import org.entando.kubernetes.model.digitalexchange.JobType;
import org.entando.kubernetes.model.web.response.SimpleRestResponse;
import org.entando.kubernetes.service.digitalexchange.job.DigitalExchangeInstallService;
import org.entando.kubernetes.service.digitalexchange.job.DigitalExchangeUninstallService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class DigitalExchangeInstallResourceController implements DigitalExchangeInstallResource {

    private final @NonNull DigitalExchangeInstallService installService;
    private final @NonNull DigitalExchangeUninstallService uninstallService;

    @Override
    public SimpleRestResponse<DigitalExchangeJob> install(
            @PathVariable("component") String componentId,
            @RequestParam(name = "version", required = true, defaultValue = "latest") String version) {

        DigitalExchangeJob installJob;
        try {
            installJob = installService.install(componentId, version);
        } catch (K8SServiceClientException ex) {
            throw new BundleNotFoundException(componentId);
        }
        return new SimpleRestResponse<>(installJob);
    }

    @Override
    public SimpleRestResponse<DigitalExchangeJob> getLastInstallJob(@PathVariable("component") String componentId) {
        DigitalExchangeJob lastInstallJob = installService.getAllJobs(componentId)
                .stream().filter(j -> JobType.matches(j.getStatus(), JobType.INSTALL))
                .findFirst()
                .orElseThrow(JobNotFoundException::new);

        return new SimpleRestResponse<>(lastInstallJob);
    }

    @Override
    public SimpleRestResponse<DigitalExchangeJob> uninstall(
            @PathVariable("component") String componentId, HttpServletRequest request) {
        return new SimpleRestResponse<>(uninstallService.uninstall(componentId));
    }

    @Override
    public SimpleRestResponse<DigitalExchangeJob> getLastUninstallJob(@PathVariable("component") String componentId) {
        DigitalExchangeJob lastUninstallJob = installService.getAllJobs(componentId)
                .stream()
                .filter(j -> JobType.matches(j.getStatus(), JobType.UNINSTALL))
                .findFirst()
                .orElseThrow(JobNotFoundException::new);
        return new SimpleRestResponse<>(lastUninstallJob);
    }

}
