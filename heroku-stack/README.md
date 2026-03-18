# Heroku Stack Examples

These examples use [Heroku Cloud Native Buildpacks](https://github.com/heroku/cnb-builder-images).
Pass `--buildpack-stack=heroku` with `nctl` to select this stack.

## Go

A basic Go web application:

```bash
nctl create application go \
  --git-url=https://github.com/ninech/deploio-examples \
  --git-sub-path=heroku-stack/go \
  --buildpack-stack=heroku
```

## Ruby on Rails

This requires the `rails` command to be installed for the `SECRET_KEY_BASE`.
If you don't have it, any long random string will do (127+ chars).

```bash
nctl create application rails \
  --git-url=https://github.com/ninech/deploio-examples \
  --git-sub-path=heroku-stack/ruby/rails-basic \
  --buildpack-stack=heroku \
  --env=SECRET_KEY_BASE=$(rails secret)
```

## Ruby on Rails with Active Storage

A Rails application with Active Storage configured for Deploio Object Storage.
Once deployed, head to the root page to test the upload functionality. The app
supports direct uploads from the browser (CORS configuration required, see
below) and custom bucket hostnames (DNS change required, see below).

```bash
# setup postgres database
nctl create postgresdatabase rails-active-storage-db --location=nine-es34 --wait
DATABASE_URL="$(nctl get postgresdatabase rails-active-storage-db --print-connection-string)"

# setup application
nctl create application rails-active-storage \
  --git-url=https://github.com/ninech/deploio-examples \
  --git-sub-path=heroku-stack/ruby/rails-active-storage \
  --buildpack-stack=heroku \
  --env=SECRET_KEY_BASE=$(rails secret)\;DATABASE_URL=${DATABASE_URL}

# create bucket
nctl create bucket --location=nine-es34 rails-active-storage-bucket

# create bucket user
nctl create bucketuser --location=nine-es34 rails-active-storage-bucketuser

# grant bucket user read & write access to bucket
nctl update bucket rails-active-storage-bucket --permissions reader=rails-active-storage-bucketuser \
  --permissions writer=rails-active-storage-bucketuser

# update application with object storage credentials
ACCESS_KEY="$(nctl get bucketuser rails-active-storage-bucketuser --print-credentials -o json | jq -r .s3_access_key)"
SECRET_KEY="$(nctl get bucketuser rails-active-storage-bucketuser --print-credentials -o json | jq -r .s3_secret_key)"
nctl update app rails-active-storage --env="S3_ACCESS_KEY=${ACCESS_KEY};S3_SECRET_KEY=${SECRET_KEY};S3_ENDPOINT=https://es34.objects.nineapis.ch;S3_BUCKET=rails-active-storage-bucket"

# optional: custom bucket hostnames
nctl update bucket rails-active-storage-bucket --custom-hostnames={S3_BUCKET_HOST}
nctl update app rails-active-storage --env="S3_BUCKET_HOST={S3_BUCKET_HOST}"
nctl get bucket rails-active-storage-bucket --output="yaml" # setup DNS

# CORS configuration for direct uploads from browser
# see https://guides.rubyonrails.org/active_storage_overview.html#cross-origin-resource-sharing-cors-configuration
APP_HOST=$(nctl get app rails-active-storage -o json | jq -r ".status.atProvider.defaultURLs | first")
nctl update bucket rails-active-storage-bucket \
  --cors origins=${APP_HOST} \
  --cors allowed-headers=Content-Type,Content-MD5,Content-Disposition \
  --cors response-headers=Content-Type,Content-MD5,Content-Disposition,ETag \
  --cors max-age=3600
```

## Ruby - Sinatra on Falcon

A Ruby application using Sinatra with the Falcon web server:

```bash
nctl create app falcon-features-sinatra \
     --git-url=https://github.com/ninech/deploio-examples \
     --git-sub-path=heroku-stack/ruby/falcon-sinatra \
     --buildpack-stack=heroku
```

## Node.js

A Next.js application:

```bash
nctl create application nextjs \
  --git-url=https://github.com/ninech/deploio-examples \
  --git-sub-path=heroku-stack/nodejs/nextjs \
  --buildpack-stack=heroku \
```

## PHP

A plain PHP application without a framework:

```bash
nctl create application php-plain \
  --git-url=https://github.com/ninech/deploio-examples \
  --git-sub-path=heroku-stack/php/plain \
  --buildpack-stack=heroku
```

A Symfony sample application that uses all currently available Deploio services.
You will need to set up a database, a key value store and object storage as
explained in the [tutorial](https://docs.deplo.io/quick-start/php/create_app).

```bash
nctl create application symfony \
  --git-url=https://github.com/ninech/deploio-examples \
  --git-sub-path=heroku-stack/php/symfony \
  --buildpack-stack=heroku
```

## Python

A Django application that shows a random message on every page reload. It uses
a temporary local SQLite database — the database is recreated on every
deployment or restart, so it is only useful for demonstration purposes. For
persistent data use a Postgres or MySQL external database.

The Django admin interface is available at `https://<URL of app>/admin`. Use
the credentials you pass via the env variables below to log in.

```bash
nctl create application django-example \
  --git-url=https://github.com/ninech/deploio-examples \
  --git-sub-path=heroku-stack/python/django \
  --buildpack-stack=heroku \
  --build-env=SECRET_KEY=<VERY LONG RANDOM SECRET KEY> \
  --env=DJANGO_SU_NAME=admin \
  --env=DJANGO_SU_EMAIL=admin@example.com \
  --env=DJANGO_SU_PASSWORD=<INSERT A PASSWORD HERE> \
  --env=SECRET_KEY=<VERY LONG RANDOM SECRET KEY>
```

## Static

A plain `index.html` served as a static site:

```bash
nctl create application static-html \
  --git-url=https://github.com/ninech/deploio-examples \
  --git-sub-path=heroku-stack/static/html \
  --buildpack-stack=heroku \
  --language=static
```

A frontend React app built with `npm`:

```bash
nctl create application static-react \
  --git-url=https://github.com/ninech/deploio-examples \
  --git-sub-path=heroku-stack/static/react \
  --buildpack-stack=heroku \
  --language=static
```

## KVS

A Go application that demonstrates integration with an
[On-Demand Key-Value Store](https://docs.nine.ch/docs/on-demand-databases/on-demand-key-value-store/).
It connects to the KVS on startup, writes a message, and displays the stored value on the web page.

First create a KVS instance (or use the [Cockpit](https://cockpit.nine.ch/en) interface):

```bash
nctl create kvs <kvs-name>
```

Retrieve the connection details and create the app:

```bash
export KVS_PASSWORD=$(nctl get kvs test --print-token)
export KVS_HOST=$(nctl get kvs test -o yaml | yq '.status.atProvider.fqdn')
nctl create application go \
  --git-url=https://github.com/ninech/deploio-examples \
  --git-sub-path=heroku-stack/kvs \
  --buildpack-stack=heroku \
  --env="KVS_HOST=$KVS_HOST;KVS_PASSWORD=$KVS_PASSWORD"
```

For more details see the [Key-Value Store docs](https://docs.nine.ch/docs/on-demand-databases/on-demand-key-value-store#connecting).
