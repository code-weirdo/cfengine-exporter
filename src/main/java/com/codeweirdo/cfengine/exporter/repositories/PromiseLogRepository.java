package com.codeweirdo.cfengine.exporter.repositories;

import com.codeweirdo.cfengine.exporter.domain.PromiseLog;
import com.codeweirdo.cfengine.exporter.domain.PromiseOutcome;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;

public interface PromiseLogRepository extends JpaRepository<PromiseLog, String> {
    @Query("SELECT count(pl) FROM PromiseLog pl WHERE pl.hostKey = :hostKey AND pl.promiseOutcome = :promiseOutcome AND pl.changeTimestamp BETWEEN :start AND :end")
    Long countByHostKeyAndPromiseOutcome(String hostKey, PromiseOutcome promiseOutcome, Instant start, Instant end);
}
