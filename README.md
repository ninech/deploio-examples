# deploio-examples

This repository hosts example apps for different languages and frameworks that
are being supported by deplo.io.

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

```bash
nctl create application django \
  --git-url=https://github.com/ninech/deploio-examples \
  --git-sub-path=python/django
```
