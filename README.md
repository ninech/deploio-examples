# deploio-examples

This repository hosts example applications for different languages and frameworks
supported by [Deploio](https://docs.deplo.io/). More information can be found in the
[Deploio User Guides](https://docs.deplo.io/) or in the
[Nine Platform Reference](https://docs.nine.ch/docs/category/deploio-paas).

## How to build and deploy

Deploio supports two approaches for building applications:

**Buildpacks** — Deploio automatically detects your language and framework and
builds the app without a Dockerfile. Two buildpack stacks are available:

- [`paketo-stack/`](paketo-stack/) — the default stack, using [Paketo](https://paketo.io/) buildpacks
- [`heroku-stack/`](heroku-stack/) — uses [Heroku Cloud Native Buildpacks](https://github.com/heroku/cnb-builder-images); select it with `--buildpack-stack=heroku`

**Dockerfile** — bring your own `Dockerfile` for full control over the build.
This approach is stack-independent:

- [`dockerfile/`](dockerfile/) — examples for languages not covered by buildpacks (e.g. Rust, Java)
