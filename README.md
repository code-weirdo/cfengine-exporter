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
$ java -version
~~~

### 3 - CFEngine Exporter

Download the CFEngine Exporter:

~~~
$ wget https://github.com/code-weirdo/cfengine-exporter/releases/download/v1.0.1/cfengine-exporter-1.0.1.jar -O cfengine-exporter.jar
~~~

Run the CFEngine Exporter:

~~~
$ java -jar cfengine-exporter.jar
~~~

Or optionally, to create a service:

~~~
$ cat <<EOF >> /usr/lib/systemd/system/cfengine-exporter.service
[Unit]
Description=CFEngine Exporter
After=network.target
 
[Service]
Type=simple
Restart=on-failure
RestartSec=10
ExecStart=/bin/java -Xms64m -Xmx64m -jar cfengine-exporter.jar

[Install]
WantedBy=multi-user.target
EOF
~~~

~~~
$ systemctl daemon-reload
$ systemctl start cfengine-exporter
$ systemctl status cfengine-exporter
~~~

In another terminal:

~~~
$ curl localhost:9191/actuator/prometheus | grep cfengine
# HELP cfengine_lastseen_timestamp The last time a host was seen by the CFEngine Hub
# TYPE cfengine_lastseen_timestamp gauge
cfengine_lastseen_timestamp{ip="192.168.1.1",} 1.610116528E9
cfengine_lastseen_timestamp{ip="192.168.1.1",} 1.610116528E9
...
~~~

