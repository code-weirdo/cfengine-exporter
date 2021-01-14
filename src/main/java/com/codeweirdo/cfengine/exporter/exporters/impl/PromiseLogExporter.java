package com.codeweirdo.cfengine.exporter.exporters.impl;

import com.codeweirdo.cfengine.exporter.domain.PromiseOutcome;
import com.codeweirdo.cfengine.exporter.exporters.Exporter;
import com.codeweirdo.cfengine.exporter.model.HostData;
import com.codeweirdo.cfengine.exporter.model.HostDataRegistry;
import com.codeweirdo.cfengine.exporter.repositories.PromiseExecutionRepository;
import com.codeweirdo.cfengine.exporter.repositories.PromiseLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;

@Slf4j
@Component
public class PromiseLogExporter implements Exporter {

    @Value("${scraper.state.path:/var/cfengine/state}")
    private Path statePath;
    @Value("${scraper.interval.millis}")
    private long scraperIntervalMillis;
    @Autowired
    private HostDataRegistry hostDataRegistry;
    @Autowired
    private PromiseExecutionRepository promiseExecutionRepository;
    @Autowired
    private PromiseLogRepository promiseLogRepository;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    ConfigurableApplicationContext applicationContext;

    private final int MAX_MISSED_SCRAPE_INTERVALS = 3;
    private final String SCRAPE_FILE_NAME = "scrape_state.json";
    private ScrapeState scrapeState;

    @Override
    public void initialise() {
        scrapeState = loadScrapeState(statePath);
    }

    @Override
    public void poll() {
        update();
    }

    private void update() {
        log.trace("Starting updated[{}]", PromiseLogExporter.class.getName());

        Instant now = Instant.now();
        Instant lastCheckTime = scrapeState.getLastScrapeTime();
        Instant lastAllowableCheckTime = now.minus(Duration.ofMillis(scraperIntervalMillis * MAX_MISSED_SCRAPE_INTERVALS));

        if (lastCheckTime.isAfter(lastAllowableCheckTime)) {
            hostDataRegistry.getHostKeys()
                    .forEach(hostKey -> {
                        HostData hostData = hostDataRegistry.getHostData(hostKey);
                        // Update the gauges that show the state of the last run through of policies
                        hostData.setPromisesKept(promiseExecutionRepository.countByHostKeyAndPromiseOutcome(hostKey, PromiseOutcome.KEPT));
                        hostData.setPromisesNotKept(promiseExecutionRepository.countByHostKeyAndPromiseOutcome(hostKey, PromiseOutcome.NOTKEPT));
                        hostData.setPromisesRepaired(promiseExecutionRepository.countByHostKeyAndPromiseOutcome(hostKey, PromiseOutcome.REPAIRED));
                        // Update the counters with the number of changes that have been made to the host
                        hostData.incrementChangesNotKept(promiseLogRepository.countByHostKeyAndPromiseOutcome(hostKey, PromiseOutcome.NOTKEPT, lastCheckTime, now));
                        hostData.incrementChangesRepaired(promiseLogRepository.countByHostKeyAndPromiseOutcome(hostKey, PromiseOutcome.REPAIRED, lastCheckTime, now));
                    });
        }
        scrapeState.setLastScrapeTime(now);
        storeScrapeState(statePath, scrapeState);
        hostDataRegistry.refesh();

        log.trace("Completed updated[{}]", PromiseLogExporter.class.getName());
    }

    @Data
    @NoArgsConstructor
    public static class ScrapeState {
        private Instant lastScrapeTime = Instant.EPOCH;
    }

    private ScrapeState loadScrapeState(Path path) {
        File stateFile = new File(String.format("%s/%s", path.toString(), SCRAPE_FILE_NAME));
        try {
            return objectMapper.readValue(stateFile, ScrapeState.class);
        } catch (IOException ioException) {
            log.info("Could not load the last scrape state from: {}", stateFile.getAbsolutePath());
            return new ScrapeState();
        }
    }

    private void storeScrapeState(Path path, ScrapeState scrapeState) {
        File stateFile = new File(String.format("%s/%s", path.toString(), SCRAPE_FILE_NAME));
        try {
            objectMapper.writeValue(stateFile, scrapeState);
        } catch (IOException ioException) {
            log.error("Could not store scrape state to file: {}", stateFile.getAbsolutePath(), ioException);
            applicationContext.close();
        }
    }

}
