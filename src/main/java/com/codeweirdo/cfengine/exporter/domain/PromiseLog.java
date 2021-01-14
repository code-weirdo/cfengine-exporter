package com.codeweirdo.cfengine.exporter.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.time.Instant;

@Data
@NoArgsConstructor
@Entity
@Table(name = "__promiselog")
@TypeDef(name = "promise_outcome", typeClass = PromiseOutcomeType.class)
public class PromiseLog {
    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "hostkey")
    private String hostKey;

    @Column(name = "changetimestamp")
    private Instant changeTimestamp;

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
    @Column(name = "promiseoutcome", columnDefinition = "promise_outcome")
    @Enumerated(EnumType.STRING)
    @Type( type = "promise_outcome" )
    private PromiseOutcome promiseOutcome;
    @Column(name = "linenumber")
    private String lineNumber;
    @Column(name = "policyfilehash")
    private String policyFileHash;
    @Column(name = "logmessages")
    private String logMessages;
    @Column(name = "promisees")
    private String promisees;
}
