# Java app based on Spring Boot with Redis-compatible Key-Value Store

This is an example of a URL shortener that tracks visitor numbers, offers a simple HTML form and provides a temporal overview of created short URLs.

It uses an [On-Demand Key-Value Store](https://docs.nine.ch/de/docs/on-demand-databases/on-demand-key-value-store/), where all application data is persisted.

Furthermore, the running app does not maintain any persistent state, making it [stateless](https://12factor.net/processes).
This enables horizontal scaling through the deployment of multiple, independent process units (replicas).
Replicas can be added and removed as needed, without downtime.

## Setup

Create Key-Value Store (also available in [Nine Cockpit](https://cockpit.nine.ch/en/databases/storage/keyvaluestores/new)):

```bash
  nctl create keyvaluestore urlshortener
```

Create Deploio application:

```bash
  nctl create application urlshortener \
  --git-url=https://github.com/ninech/deploio-examples \
  --git-sub-path=dockerfile/java-kvs \
  --replicas=2 \
  --env=KVS_HOST=<KEY-VALUE STORE HOSTNAME FROM COCKPIT OR NCTL> \
  --env=KVS_PASSWORD=<KEY-VALUE STORE PASSWORD FROM COCKPIT OR NCTL> \
  --env=KVS_SSL_ENABLED=true \
  --dockerfile
```
