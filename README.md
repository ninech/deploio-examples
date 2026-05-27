# deploio-examples

This repository hosts example applications for different languages and frameworks
supported by [Deploio](https://docs.deplo.io/). More information can be found in the
[Deploio User Guides](https://docs.deplo.io/) or in the
[Nine Platform Reference](https://docs.nine.ch/docs/category/deploio-paas).

## How to build and deploy

Deploio supports two approaches for building applications:

**Buildpacks** — Deploio automatically detects your language and framework and
builds the app without a Dockerfile. Two buildpack stacks are available:

- [`heroku-stack/`](heroku-stack/) — the default stack, using [Heroku Cloud Native Buildpacks](https://github.com/heroku/cnb-builder-images)
- [`paketo-stack/`](paketo-stack/) — uses [Paketo](https://paketo.io/) buildpacks; select it with `--buildpack-stack=paketo`

**Dockerfile** — bring your own `Dockerfile` for full control over the build.
This approach is stack-independent:

- [`dockerfile/`](dockerfile/) — examples for languages not covered by buildpacks (e.g. Rust, Java)
