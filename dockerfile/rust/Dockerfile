FROM rust:slim AS build-env
WORKDIR /app
COPY . /app
RUN --mount=type=cache,target=/usr/local/cargo/registry \
    --mount=type=cache,target=/app/target \
    cargo build --release
RUN --mount=type=cache,target=/app/target cp /app/target/release/deploio-rust-example /tmp

FROM gcr.io/distroless/cc-debian12
COPY --from=build-env /tmp/deploio-rust-example /
COPY Rocket.toml /
CMD ["./deploio-rust-example"]
