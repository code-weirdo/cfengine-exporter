package com.codeweirdo.cfengine.exporter.model;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class HostDataRegistry {

    @Autowired
    private MeterRegistry meterRegistry;
    private final Map<String, HostData> hostData = new ConcurrentHashMap<>();

    public Collection<String> getHostKeys() {
        return hostData.keySet();
    }

    public HostData getHostData(String hostKey) {
        return hostData.get(hostKey);
    }

    public HostData createEntryIfMissing(String key) {
        hostData.computeIfAbsent(key, (k) -> new HostData(meterRegistry));
        return hostData.get(key);
    }

    public synchronized void refesh() {
        hostData.forEach((key, value) -> value.refreshMetrics());
    }
}

