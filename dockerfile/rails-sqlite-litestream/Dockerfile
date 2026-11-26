FROM docker.io/library/ruby:4.0-slim AS base
WORKDIR /app
ENV RAILS_ENV=production \
    BUNDLE_DEPLOYMENT=1 \
    BUNDLE_WITHOUT=development:test \
    PORT=8080

FROM base AS build
RUN apt-get update -qq && \
    apt-get install --no-install-recommends -y build-essential libyaml-dev pkg-config && \
    rm -rf /var/lib/apt/lists/*
COPY Gemfile Gemfile.lock ./
RUN bundle install && rm -rf ~/.bundle "${BUNDLE_PATH}"/ruby/*/cache

FROM base
COPY --from=build /usr/local/bundle /usr/local/bundle
COPY . .
RUN useradd --create-home rails && mkdir storage tmp && chown rails storage tmp
USER rails

EXPOSE 8080
CMD ["bundle", "exec", "litestream", "replicate", "-config", "litestream.yml"]
