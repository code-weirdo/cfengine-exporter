package com.codeweirdo.cfengine.exporter.model;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

public class HostData {
    @Getter
    @Setter
    private String host;
    @Getter
    @Setter
    private Instant lastSeen = Instant.EPOCH;
    @Getter
    @Setter
    private long promisesKept;
    @Getter
    @Setter
    private long promisesNotKept;
    @Getter
    @Setter
    private long promisesRepaired;

    // Metric Tracking
    private final MeterRegistry meterRegistry;
    private Gauge lastSeenGauge;
    private Gauge promisesKeptGauge;
    private Gauge promisesNotKeptGauge;
    private Gauge promisesRepairedGauge;
    private Counter changesNotKeptCounter;
    private Counter changesRepairedCounter;

    public HostData(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void updateLastSeen(Instant instant) {
        assert(instant != null);
        if (lastSeen == null) {
            lastSeen = instant;
        }
        lastSeen = instant.isAfter(lastSeen) ? instant : lastSeen;
    }

    public void incrementChangesNotKept(long count) {
        if (changesNotKeptCounter != null) {
            changesNotKeptCounter.increment(count);
        }
    }

    public void incrementChangesRepaired(long count) {
        if (changesRepairedCounter != null) {
            changesRepairedCounter.increment(count);
        }
    }

    public void refreshMetrics() {
        if (host != null) {
            if (lastSeenGauge == null) {
                lastSeenGauge = Gauge
                        .builder("cfengine.lastseen.timestamp", () -> lastSeen.getEpochSecond())
                        .description("The last time a host was seen by the CFEngine Hub")
                        .tags("ip", host)
                        .register(meterRegistry);
            }
            if ((promisesKeptGauge == null) && (promisesUpdated())) {
                promisesKeptGauge = Gauge.builder("cfengine.promises.kept", () -> promisesKept)
                        .description("")
                        .tags("ip", host)
                        .register(meterRegistry);
            }
            if ((promisesNotKeptGauge == null) && (promisesUpdated())) {
                promisesNotKeptGauge = Gauge.builder("cfengine.promises.notkept", () -> promisesNotKept)
                        .description("")
                        .tags("ip", host)
                        .register(meterRegistry);
            }
            if ((promisesRepairedGauge == null) && (promisesUpdated())) {
                promisesRepairedGauge = Gauge.builder("cfengine.promises.repaired", () -> promisesRepaired)
                        .description("")
                        .tags("ip", host)
                        .register(meterRegistry);
            }
            if (changesNotKeptCounter == null) {
                changesNotKeptCounter = Counter
                        .builder("cfengine.changes.notkept.total")
                        .description("The number of changes on this host that were not kept by CFEngine")
                        .tag("ip", host)
                        .register(meterRegistry);
            }
            if (changesRepairedCounter == null) {
                changesRepairedCounter = Counter
                        .builder("cfengine.promises.repaired.total")
                        .description("The number of changes that were repaired by CFEngine")
                        .tag("ip", host)
                        .register(meterRegistry);
            }
        }
    }

    private boolean promisesUpdated() {
        return (promisesKept + promisesNotKept + promisesRepaired) > 0;
    }
}
