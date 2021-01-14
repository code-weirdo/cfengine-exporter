package com.codeweirdo.cfengine.exporter.repositories;

import com.codeweirdo.cfengine.exporter.domain.Host;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HostRepository extends JpaRepository<Host, String> {

}
