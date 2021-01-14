package com.codeweirdo.cfengine.exporter;

import com.codeweirdo.cfengine.exporter.exporters.Exporter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.stream.Stream;

@SpringBootApplication
@EntityScan
@EnableJpaRepositories
@EnableScheduling
public class CFEngineExporter {

    public static void main(String[] args) {
        Stream.of(args).forEach(System.out::println);
        SpringApplication.run(CFEngineExporter.class, args);
    }

    @Autowired
    private List<Exporter> availableExporters;

    @PostConstruct
    public void initialise() {
        availableExporters.forEach(Exporter::initialise);
    }

    @Scheduled(initialDelayString = "${scraper.interval.millis}", fixedRateString = "${scraper.interval.millis}")
    public void poll() {
        availableExporters.forEach(Exporter::poll);
    }

}
