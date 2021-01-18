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
import java.time.temporal.ChronoUnit;

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
    private final int REPORTING_LATENCY_MINUTES = 15;
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
        log.debug("Starting updated[{}]", PromiseLogExporter.class.getName());

        Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        Instant lastCheckTime = scrapeState.getLastScrapeTime();
        Instant lastAllowableCheckTime = now.minus(Duration.ofMillis(scraperIntervalMillis * MAX_MISSED_SCRAPE_INTERVALS));

        if (lastCheckTime.isAfter(lastAllowableCheckTime)) {
            log.debug("Checking promise logs for updates between {} and {}", lastCheckTime, now);
            hostDataRegistry.getHostKeys()
                    .forEach(hostKey -> {
                        HostData hostData = hostDataRegistry.getHostData(hostKey);
                        log.debug("[{}] Checking promise logs for host: {}", hostData.getHost(), hostKey);
                        // Update the gauges that show the state of the last run through of policies
                        long promisesKept = promiseExecutionRepository.countByHostKeyAndPromiseOutcome(hostKey, PromiseOutcome.KEPT);
                        long promisesNotKept = promiseExecutionRepository.countByHostKeyAndPromiseOutcome(hostKey, PromiseOutcome.NOTKEPT);
                        long promisesRepaired = promiseExecutionRepository.countByHostKeyAndPromiseOutcome(hostKey, PromiseOutcome.REPAIRED);
                        log.debug("[{}] - Promises: Kept[{}], Not Kept[{}], Repaired[{}]", hostData.getHost(), promisesKept, promisesNotKept, promisesRepaired);
                        hostData.setPromisesKept(promisesKept);
                        hostData.setPromisesNotKept(promisesNotKept);
                        hostData.setPromisesRepaired(promisesRepaired);
                        // Update the counters with the number of changes that have been made to the host
                        Instant startTime = lastCheckTime.minus(Duration.ofMinutes(REPORTING_LATENCY_MINUTES));
                        Instant endTime = now.minus(Duration.ofMinutes(REPORTING_LATENCY_MINUTES));
                        long changesNotKept = promiseLogRepository.countByHostKeyAndPromiseOutcome(hostKey, PromiseOutcome.NOTKEPT, startTime, endTime);
                        long changesRepaired = promiseLogRepository.countByHostKeyAndPromiseOutcome(hostKey, PromiseOutcome.REPAIRED, startTime, endTime);
                        log.debug("[{}] - Changes: Not Kept[{}], Repaired[{}]", hostData.getHost(), changesNotKept, changesRepaired);
                        hostData.incrementChangesNotKept(changesNotKept);
                        hostData.incrementChangesRepaired(changesRepaired);
                    });
        } else {
            log.debug("Outside allowable check time {}, syncing...", lastAllowableCheckTime);
        }

        scrapeState.setLastScrapeTime(now);
        storeScrapeState(statePath, scrapeState);
        hostDataRegistry.refresh();

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
