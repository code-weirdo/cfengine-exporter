package com.codeweirdo.cfengine.exporter.exporters.impl;

import com.codeweirdo.cfengine.exporter.domain.LastSeenHost;
import com.codeweirdo.cfengine.exporter.exporters.Exporter;
import com.codeweirdo.cfengine.exporter.model.HostData;
import com.codeweirdo.cfengine.exporter.model.HostDataRegistry;
import com.codeweirdo.cfengine.exporter.repositories.HostRepository;
import com.codeweirdo.cfengine.exporter.repositories.LastSeenHostsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Slf4j
@Component
public class LastSeenExporter implements Exporter {

    @Autowired
    HostDataRegistry hostDataRegistry;
    @Autowired
    private HostRepository hostRepository;
    @Autowired
    private LastSeenHostsRepository lastSeenHostsRepository;

    @Override
    public void initialise() {
        update();
    }

    @Override
    public void poll() {
        update();
    }

    private void update() {
        log.debug("Starting updated[{}]", LastSeenExporter.class.getName());
        hostRepository.findAll()
                .forEach(host -> {
                    List<LastSeenHost> lastSeenHostEntries = lastSeenHostsRepository.findByRemoteHostKey(host.getHostKey());
                    HostData hostData = hostDataRegistry.getHostData(host.getHostKey());
                    if (lastSeenHostEntries.size() > 0) {
                        updateIpAddress(lastSeenHostEntries, hostData);
                        updateLastSeen(lastSeenHostEntries, hostData);
                        log.debug("[{}]: last seen updated to {}", hostData.getHost(), hostData.getLastSeen());
                    }
                    else {
                        log.debug("[{}]: No last seen entries for host [{}]", hostData.getHost(), host.getHostKey());
                    }
                });
        hostDataRegistry.refresh();
        log.debug("Update completed[{}]", LastSeenExporter.class.getName());
    }

    private void updateIpAddress(List<LastSeenHost> lastSeenHostEntries, HostData hostData) {
        lastSeenHostEntries.stream()
                .map(LastSeenHost::getRemoteHostIPAddress)
                .findFirst()
                .ifPresent(hostData::setHost);
    }

    private void updateLastSeen(List<LastSeenHost> lastSeenHostEntries, HostData hostData) {
        Instant lastSeen = lastSeenHostEntries.stream()
                .map(LastSeenHost::getLastSeenInstant)
                .reduce(Instant.EPOCH, (result, seen) -> seen.isAfter(result) ? seen : result);
        if (lastSeen.isAfter(Instant.EPOCH)) {
            hostData.updateLastSeen(lastSeen);
        }
    }

}
