package com.codeweirdo.cfengine.exporter.repositories;

import com.codeweirdo.cfengine.exporter.domain.LastSeenHost;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LastSeenHostsRepository extends JpaRepository<LastSeenHost, String> {

    List<LastSeenHost> findByHostKey(String hostKey);
    List<LastSeenHost> findByRemoteHostKey(String remoteHostKey);

}
