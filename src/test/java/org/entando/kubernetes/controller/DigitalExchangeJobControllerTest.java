package org.entando.kubernetes.controller;


import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import org.entando.kubernetes.EntandoKubernetesJavaApplication;
import org.entando.kubernetes.config.TestKubernetesConfig;
import org.entando.kubernetes.config.TestSecurityConfiguration;
import org.entando.kubernetes.model.digitalexchange.DigitalExchangeJob;
import org.entando.kubernetes.model.digitalexchange.JobStatus;
import org.entando.kubernetes.model.digitalexchange.JobType;
import org.entando.kubernetes.repository.DigitalExchangeJobRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.data.domain.Example;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(
        webEnvironment = WebEnvironment.RANDOM_PORT,
        classes = {EntandoKubernetesJavaApplication.class, TestSecurityConfiguration.class, TestKubernetesConfig.class})
@AutoConfigureMockMvc
@ActiveProfiles({"test"})
@Tag("component")
public class DigitalExchangeJobControllerTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    private DigitalExchangeJobRepository jobRepository;

    Map<UUID, DigitalExchangeJob> jobs;

    @BeforeEach
    public void setup() {
        populateTestDatabase();
    }

    @AfterEach
    public void teardown() {
       jobRepository.deleteAll();
    }

    @Test
    public void shouldReturnAllJobsSortedByFinishTime() throws Exception {

        mvc.perform(get("/jobs").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payload").value(hasSize(4)))
                .andExpect(jsonPath("$.payload[0].componentId").value("id1"))
                .andExpect(jsonPath("$.payload[0].status").value("INSTALL_COMPLETED"))
                .andExpect(jsonPath("$.payload[0].startedAt").value("2020-03-05T14:30:00"))
                .andExpect(jsonPath("$.payload[1].componentId").value("id1"))
                .andExpect(jsonPath("$.payload[1].status").value("UNINSTALL_COMPLETED"))
                .andExpect(jsonPath("$.payload[1].startedAt").value("2020-02-02T07:23:00"))
                .andExpect(jsonPath("$.payload[2].componentId").value("id2"))
                .andExpect(jsonPath("$.payload[2].status").value("INSTALL_IN_PROGRESS"))
                .andExpect(jsonPath("$.payload[2].startedAt").value("2020-01-14T07:23:00"))
                .andExpect(jsonPath("$.payload[3].componentId").value("id1"))
                .andExpect(jsonPath("$.payload[3].status").value("INSTALL_COMPLETED"))
                .andExpect(jsonPath("$.payload[3].startedAt").value("2020-01-10T10:30:00"));
    }

    @Test
    public void shouldReturnJobById() throws Exception {

        DigitalExchangeJob job = jobs.entrySet().stream().findFirst().map(Entry::getValue).get();

        mvc.perform(get("/jobs/{id}", job.getId()).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payload.componentId").value(job.getComponentId()))
                .andExpect(jsonPath("$.payload.status").value(job.getStatus().toString()));
    }

    @Test
    public void shouldReturnNotFoundWithNonExistentId() throws Exception {

        UUID jobId = UUID.randomUUID();

        mvc.perform(get("/jobs/{id}", jobId).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldReturnLastJobOfType() throws Exception {

        String componentId = "id1";
        JobType jobType = JobType.INSTALL;

        mvc.perform(get("/jobs?component={component}&type={type}", componentId,jobType).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payload.componentId").value(componentId))
                .andExpect(jsonPath("$.payload.status").value(JobStatus.INSTALL_COMPLETED.toString()))
                .andExpect(jsonPath("$.payload.finishedAt").value("2020-03-05T14:32:00"));
    }

    @Test
    public void shouldReturnLastJobWithStatus() throws Exception {

        String componentId = "id1";
        JobStatus jobStatus = JobStatus.UNINSTALL_COMPLETED;

        mvc.perform(get("/jobs?component={component}&status={status}", componentId,jobStatus).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payload.componentId").value(componentId))
                .andExpect(jsonPath("$.payload.status").value("UNINSTALL_COMPLETED"))
                .andExpect(jsonPath("$.payload.startedAt").value("2020-02-02T07:23:00"))
                .andExpect(jsonPath("$.payload.finishedAt").value("2020-02-02T07:23:30"));
    }

    private void populateTestDatabase() {
        jobs = new HashMap<>();

        DigitalExchangeJob job1 = new DigitalExchangeJob();
        job1.setComponentId("id1");
        job1.setComponentName("my-bundle");
        job1.setDigitalExchange("local");
        job1.setProgress(1.0);
        job1.setComponentVersion("1.0.0");
        job1.setStartedAt(LocalDateTime.of(2020, Month.JANUARY, 10, 10, 30));
        job1.setFinishedAt(job1.getStartedAt().plusMinutes(1L));
        job1.setStatus(JobStatus.INSTALL_COMPLETED);

        DigitalExchangeJob _job1 = jobRepository.save(job1);
        jobs.put(_job1.getId(), _job1);

        DigitalExchangeJob job2 = new DigitalExchangeJob();
        job2.setComponentId("id2");
        job2.setComponentName("my-other-bundle");
        job2.setDigitalExchange("external");
        job2.setComponentVersion("1.0.0");
        job2.setProgress(0.5);
        job2.setStartedAt(LocalDateTime.of(2020, Month.JANUARY, 14, 7, 23));
        job2.setFinishedAt(null);
        job2.setStatus(JobStatus.INSTALL_IN_PROGRESS);

        DigitalExchangeJob _job2 = jobRepository.save(job2);
        jobs.put(_job2.getId(), _job2);

        DigitalExchangeJob job1_uninstall = new DigitalExchangeJob();
        job1_uninstall.setComponentId("id1");
        job1_uninstall.setComponentName("my-bundle");
        job1_uninstall.setDigitalExchange("local");
        job1_uninstall.setComponentVersion("1.0.0");
        job1_uninstall.setProgress(1.0);
        job1_uninstall.setStartedAt(LocalDateTime.of(2020, Month.FEBRUARY, 2, 7, 23));
        job1_uninstall.setFinishedAt(job1_uninstall.getStartedAt().plusSeconds(30L));
        job1_uninstall.setStatus(JobStatus.UNINSTALL_COMPLETED);

        DigitalExchangeJob _job1_uninstall = jobRepository.save(job1_uninstall);
        jobs.put(_job1_uninstall.getId(), _job1_uninstall);

        DigitalExchangeJob job1_reinstall = new DigitalExchangeJob();
        job1_reinstall.setComponentId("id1");
        job1_reinstall.setComponentName("my-bundle");
        job1_reinstall.setDigitalExchange("local");
        job1_reinstall.setProgress(1.0);
        job1_reinstall.setComponentVersion("1.0.0");
        job1_reinstall.setStartedAt(LocalDateTime.of(2020, Month.MARCH, 5, 14, 30));
        job1_reinstall.setFinishedAt(job1_reinstall.getStartedAt().plusMinutes(2L));
        job1_reinstall.setStatus(JobStatus.INSTALL_COMPLETED);

        DigitalExchangeJob _job1_reinstall = jobRepository.save(job1_reinstall);
        jobs.put(_job1_reinstall.getId(), _job1_reinstall);

    }


}
