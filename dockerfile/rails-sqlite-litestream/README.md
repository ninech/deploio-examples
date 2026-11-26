# Rails with SQLite, replicated to Object Storage

Deploio gives an app no persistent disk, only an ephemeral filesystem. This example
still keeps its data in SQLite: the database file lives in the container, and
[Litestream](https://litestream.io) streams every committed transaction to a
Deploio Object Storage bucket, usually within a second. On start, the container
restores the database from the bucket.

The whole Rails app is [`app.rb`](app.rb), next to its `Gemfile`, Puma config and
[`litestream.yml`](litestream.yml).

## How it works

The container runs `litestream replicate` (see `Dockerfile` and `litestream.yml`):

1. Litestream restores the database from the bucket because the file is missing
   (nothing on the very first start).
2. It runs Puma. Puma loads `app.rb`, which creates the `notes` table if it is missing.
3. It streams every change to the bucket.
4. On `SIGTERM`, Puma finishes its requests and Litestream uploads the last changes.

## Deploying without two writers

Deploio rolls out a new release next to the old one and stops the old one once the
new one is up. Two Litestream processes writing the same replica
[can make it impossible to restore](https://litestream.io/tips/), and the new one
would restore before the old one uploaded its last changes. So never let a release
roll out while the app runs: pause it first, so the old container stops and uploads
everything, and unpause it once the new release is in place.

```bash
nctl update app rails-sqlite-litestream --pause
nctl update app rails-sqlite-litestream --git-revision=<commit>  # or env changes, --retry-release
nctl update app rails-sqlite-litestream --no-pause
```

The app is down while it is paused. Pin `--git-revision` to a commit or tag: an app
that follows a branch rolls out every push on its own.

## Limits

- **One replica.** `nctl create app` sets 2 replicas unless told otherwise, and that
  wins over `replicas: 1` in `.deploio.yaml`, so pass `--replicas=1`
  (or fix an existing app with `nctl update app <name> --replicas=1`).
- **Downtime on every deploy**, see above.
- **No deploy job and no worker jobs.** They run in their own containers and would see
  their own throwaway copy of the database. The app creates its table when Puma boots,
  and background jobs would have to run inside the Puma process.
- **About a second of writes can be lost** if the container is killed without `SIGTERM`
  (out of memory, node failure). A paused deploy loses nothing.
- **A container moved by the platform** (for example during node maintenance) can start
  before the old one has stopped, with the same risk as a rolling deploy.
- **The database and its WAL must fit into the 2 GiB of ephemeral storage**, and a
  larger database takes longer to restore on every start.

## Deploy

See the [Dockerfile README](../README.md#ruby-on-rails-with-sqlite-and-litestream).

## Develop

```sh
bundle install
bundle exec puma -C puma.rb
```

Locally the app uses a plain SQLite file in `storage/` without replication.

## Test

```sh
bundle exec ruby test/app_test.rb
test/redeploy_test
```

`test/redeploy_test` needs [rclone](https://rclone.org) as a local S3 server. It runs
the container's command in copies of the app, one after the other, against one bucket
and checks that every note survives a stop and a crash.
