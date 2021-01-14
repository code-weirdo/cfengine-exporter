package com.codeweirdo.cfengine.exporter.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.Instant;

@Data
@NoArgsConstructor
@Entity
@Table(name = "__hosts")
public class Host {
    @Id
    @Column(name = "hostkey")
    private String hostKey;

    @Column(name = "iscallcollected")
    private boolean isCallCollected;

    @Column(name = "lastreporttimestamp")
    private Instant lastReported;

    @Column(name = "firstreporttimestamp")
    private Instant firstReported;

    @Column(name = "hostkeycollisions")
    private int hostKeyCollisions;
}
