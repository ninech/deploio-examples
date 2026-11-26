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

## Ruby on Rails with SQLite and Litestream

**⚠️ Experimental: Attention, there are some risks you should understand before using this:**
* You must disable rolling releases (e.g. by pausing the app)
* You can have only one replica, no deploy jobs and no background workers
* You should read about [known issues](https://github.com/fractaledmind/litestream-ruby#known-issues-with-litestream-0517)
* Nine's planned maintenance windows might still allow for parallel access to your SQLite database and leave the database in a damaged state.

If you are ok with the above, continue here.

A one-file Rails application that keeps its data in SQLite on the ephemeral filesystem
and uses [Litestream](https://litestream.io) to stream every change to Object Storage.
On start it restores the database from the bucket. Run it with a single replica and
pause it for every deploy, see [its README](rails-sqlite-litestream/README.md) for why
and for its limits.

```bash
# create bucket and bucket user
nctl create bucket --location=nine-es34 rails-sqlite-litestream-bucket
nctl create bucketuser --location=nine-es34 rails-sqlite-litestream-bucketuser
nctl update bucket rails-sqlite-litestream-bucket --permissions reader=rails-sqlite-litestream-bucketuser \
  --permissions writer=rails-sqlite-litestream-bucketuser
ACCESS_KEY="$(nctl get bucketuser rails-sqlite-litestream-bucketuser --print-credentials -o json | jq -r .s3_access_key)"
SECRET_KEY="$(nctl get bucketuser rails-sqlite-litestream-bucketuser --print-credentials -o json | jq -r .s3_secret_key)"

# setup application
nctl create application rails-sqlite-litestream \
  --git-url=https://github.com/ninech/deploio-examples \
  --git-sub-path=dockerfile/rails-sqlite-litestream \
  --git-revision=<commit> \
  --build-type=dockerfile \
  --replicas=1 \
  --env="S3_ENDPOINT=https://es34.objects.nineapis.ch;S3_BUCKET=rails-sqlite-litestream-bucket" \
  --sensitive-env="SECRET_KEY_BASE=$(openssl rand -hex 64);S3_ACCESS_KEY=${ACCESS_KEY};S3_SECRET_KEY=${SECRET_KEY}"
```

Add a few notes, then deploy again with the app paused. The notes are still there afterwards:

```bash
nctl update app rails-sqlite-litestream --pause
nctl update app rails-sqlite-litestream --retry-release
nctl update app rails-sqlite-litestream --no-pause
nctl logs app rails-sqlite-litestream -f
```
