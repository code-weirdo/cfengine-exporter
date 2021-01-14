package com.codeweirdo.cfengine.exporter.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.Instant;

@Data
@NoArgsConstructor
@Entity
@Table(name = "__promiseexecutions")
public class PromiseExecution {
    @Id
    @Column(name = "hostkey")
    private String hostKey;
    @Column(name = "policyfile")
    private String policyFile;
    @Column(name = "releaseid")
    private String releaseId;
    @Column(name = "promisehash")
    private String promiseHash;
    @Column(name = "namespace")
    private String namespace;
    @Column(name = "bundlename")
    private String bundleName;
    @Column(name = "promisetype")
    private String promiseType;
    @Column(name = "promiser")
    private String promiser;
    @Column(name = "stackpath")
    private String stackPath;
    @Column(name = "promisehandle")
    private String promiseHandle;
    @Column(name = "promiseoutcome")
    @Enumerated(EnumType.STRING)
    private PromiseOutcome promiseOutcome;
    @Column(name = "linenumber")
    private String lineNumber;
    @Column(name = "policyfilehash")
    private String policyFileHash;
    @Column(name = "logmessages")
    private String logMessages;
    @Column(name = "promisees")
    private String promisees;
    @Column(name = "changetimestamp")
    private Instant changeTimestamp;
}
