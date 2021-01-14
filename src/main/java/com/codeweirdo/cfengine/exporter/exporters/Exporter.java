package com.codeweirdo.cfengine.exporter.exporters;

public interface Exporter {

    void initialise();
    void poll();

}
