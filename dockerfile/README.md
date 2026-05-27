# Dockerfile Examples

With Dockerfile builds, Deploio can build any app that can be containerized.
This approach is stack-independent — no buildpack is involved.

## Rust

A basic Rust web application:

```bash
nctl create application dockerfile-rust \
  --git-url=https://github.com/ninech/deploio-examples \
  --git-sub-path=dockerfile/rust \
  --dockerfile
```

## Java (Spring Boot + KVS)

A Java app based on Spring Boot that integrates with an
[On-Demand Key-Value Store](https://docs.nine.ch/de/docs/on-demand-databases/on-demand-key-value-store/):

```bash
nctl create application dockerfile-java-kvs \
  --git-url=https://github.com/ninech/deploio-examples \
  --git-sub-path=dockerfile/java-kvs \
  --env=KVS_HOST=<KEY-VALUE STORE HOSTNAME FROM COCKPIT OR NCTL> \
  --env=KVS_PASSWORD=<KEY-VALUE STORE PASSWORD FROM COCKPIT OR NCTL> \
  --env=KVS_SSL_ENABLED=true \
  --dockerfile
```

See [java-kvs/README.md](java-kvs/README.md) for full setup instructions.
