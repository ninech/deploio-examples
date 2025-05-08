# deploio-examples

This repository hosts example apps for different languages and frameworks that
are being supported by Deploio. Please also check out our [language specific
documentation](https://docs.nine.ch/docs/category/languages) for more details.

## Go

```bash
nctl create application go \
  --git-url=https://github.com/ninech/deploio-examples \
  --git-sub-path=go
```

## Ruby

This requires the `rails` command to be installed for the `SECRET_KEY_BASE`.
If you don't have it, any long random string will do (127+ chars).

```bash
nctl create application rails \
  --git-url=https://github.com/ninech/deploio-examples \
  --git-sub-path=ruby/rails-basic \
  --env=SECRET_KEY_BASE=$(rails secret)
```

## Node.js

```bash
nctl create application nextjs \
  --git-url=https://github.com/ninech/deploio-examples \
  --git-sub-path=nodejs/nextjs \
  --build-env=NODE_ENV="production" \
  --env=NODE_ENV="production"
```

## PHP

```bash
nctl create application symfony \
  --git-url=https://github.com/ninech/deploio-examples \
  --git-sub-path=php/symfony
```

## Python

The example provides a Django application which shows a random message on every
page reload. It uses a temporary local sqlite database. Please note that the
database will be recreated on every deployment or restart of the application
(all data will be lost), so it really just is useful for demonstration purposes.
For persistent data, please use a postgres or mysql external database.  The
Django admin interface can be used to add messages. Just visit `https://<URL of
app>/admin` to access it and use the user credentials which you pass via the env
variables below to login.
Please also define the `SECRET_KEY` which is used to secure signed data and
should be kept secret.

```bash
nctl create application django-example \
  --git-url=https://github.com/ninech/deploio-examples \
  --git-sub-path=python/django \
  --env=DJANGO_SU_NAME=admin \
  --env=DJANGO_SU_EMAIL=admin@example.com \
  --env=DJANGO_SU_PASSWORD=<INSERT A PASSWORD HERE> \
  --env=SECRET_KEY=<VERY LONG RANDOM SECRET KEY>
```

## Static

For static sites we have two examples:

* just a plain `index.html`

    ```bash
    nctl create application static-html \
      --git-url=https://github.com/ninech/deploio-examples \
      --git-sub-path=static/html
    ```

* a frontend react app built with `npm`

    ```bash
    nctl create application static-react \
      --git-url=https://github.com/ninech/deploio-examples \
      --git-sub-path=static/react
    ```

## Dockerfile

With Dockerfile builds, Deploio can build any app that can be built using a
Dockerfile. To demonstrate this we have the following sample apps:

* a very basic Rust app:

    ```bash
    nctl create application dockerfile-rust \
    --git-url=https://github.com/ninech/deploio-examples \
    --git-sub-path=dockerfile/rust \
    --dockerfile
    ```
* a Java app based on Spring Boot that integrates with an [On-Demand Key-Value Store](https://docs.nine.ch/de/docs/on-demand-databases/on-demand-key-value-store/):

  ```bash
  nctl create application dockerfile-java-kvs \
  --git-url=https://github.com/ninech/deploio-examples \
  --git-sub-path=dockerfile/java-kvs \
  --env=KVS_HOST=<KEY-VALUE STORE HOSTNAME FROM COCKPIT OR NCTL> \
  --env=KVS_PASSWORD=<KEY-VALUE STORE PASSWORD FROM COCKPIT OR NCTL> \
  --env=KVS_SSL_ENABLED=true \
  --dockerfile
  ```

  See [dockerfile/java-kvs/README.md](dockerfile/java-kvs/README.md) for details and full setup instructions.

## KVS

This example uses an [On-Demand Key-Value Store](https://docs.nine.ch/de/docs/on-demand-databases/on-demand-key-value-store/).To get started, you'll need to first create a KVS instance - either through `nctl` or via the [Cockpit](https://cockpit.nine.ch/en) interface: 

```bash
nctl create kvs <kvs-name>
```

Once your instance is ready you can create the app using the command below. You’ll need the instance’s connection details (FQDN and TOKEN), which can be retrieved using `nctl` or found in Cockpit under “Access Information.” For more details, see [Key-Value Store docs](https://docs.nine.ch/docs/on-demand-databases/on-demand-key-value-store#connecting):

```bash
export KVSCLI_AUTH=$(nctl get kvs test --print-token)
export KVS_HOST=$(nctl get kvs test -o yaml | yq '.status.atProvider.fqdn')
nctl create application go \
  --git-url=https://github.com/ninech/deploio-examples \
  --git-sub-path=kvs \
  --env="KVS_HOST=$KVS_HOST;KVSCLI_AUTH=$KVSCLI_AUTH"
```