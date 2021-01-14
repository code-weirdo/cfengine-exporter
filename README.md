# cfengine-exporter

A Prometheus exporter for CFEngine

# Installation

### 1 - CFEngine PostreSQL Access

On the hub, make sure the CFEngine PostgreSQL database will accept traffic from localhost:
~~~
$ echo "host cfdb cfpostgres 127.0.0.1/32 trust" >> /var/cfengine/state/pg/data/pg_hba.conf
$ systemctl restart cfengine3
~~~

To test this, you should be able to connect to the database using the following command:
~~~
$ psql -U cfpostgres -h 127.0.0.1 -d cfdb
~~~

### 2 - Java

This exporter requires at least Java 1.8 installed. Install using your distro's package manager:

~~~
$ apt-get install -y openjdk-8-jre-headless
~~~

### 3 - CFEngine Exporter

Download the CFEngine Exporter:

~~~
$ wget <github-release-url> -O cfengine-exporter.jar
~~~

Run the CFEngine Exporter:

~~~
$ java -jar cfengine-exporter.jar
~~~
