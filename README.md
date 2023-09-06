# deploio-examples

This repository hosts example apps for different languages and frameworks that
are being supported by deplo.io. Please also check out our [language specific
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
  --git-sub-path=nodejs/nextjs
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
