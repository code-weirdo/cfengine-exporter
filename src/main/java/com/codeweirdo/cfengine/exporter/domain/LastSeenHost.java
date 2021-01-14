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
@Table(name = "__lastseenhosts")
public class LastSeenHost {

    @Id
    @Column(name = "hostkey")
    private String hostKey;

    @Column(name = "compo")
    private String compo;

    @Column(name = "lastseendirection")
    private String lastSeenDirection;

    @Column(name = "remotehostkey")
    private String remoteHostKey;

    @Column(name = "remotehostip")
    private String remoteHostIPAddress;

    @Column(name = "lastseentimestamp")
    private Instant lastSeenInstant;

    @Column(name = "lastseeninterval")
    private Long lastSeenInterval;
}
