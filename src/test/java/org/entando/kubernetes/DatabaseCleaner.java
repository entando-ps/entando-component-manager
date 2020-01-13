package org.entando.kubernetes;

import org.entando.kubernetes.repository.DigitalExchangeJobComponentRepository;
import org.entando.kubernetes.repository.DigitalExchangeJobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DatabaseCleaner {

    @Autowired
    DigitalExchangeJobRepository jobRepository;

    @Autowired
    DigitalExchangeJobComponentRepository jobComponentRepository;

    public void cleanup() {
        jobComponentRepository.deleteAll();
        jobRepository.deleteAll();
    }

}
