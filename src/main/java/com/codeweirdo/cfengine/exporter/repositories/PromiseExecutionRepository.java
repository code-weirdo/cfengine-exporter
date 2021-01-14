package com.codeweirdo.cfengine.exporter.repositories;

import com.codeweirdo.cfengine.exporter.domain.PromiseExecution;
import com.codeweirdo.cfengine.exporter.domain.PromiseOutcome;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PromiseExecutionRepository extends JpaRepository<PromiseExecution, String> {
    long countByHostKeyAndPromiseOutcome(String hostKey, PromiseOutcome promiseOutcome);
}
