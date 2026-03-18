#!/usr/bin/env -S falcon host
# frozen_string_literal: true

require "falcon/environment/rack"

hostname = File.basename(__dir__)
port = ENV["PORT"] || 3000

service hostname do
  include Falcon::Environment::Rack

  count ENV.fetch("WEB_CONCURRENCY", 1).to_i

  preload "preload.rb"

  # Use HTTP/1.1 for internal backend compatibility with most K8s ingress controllers
  endpoint Async::HTTP::Endpoint
             .parse("http://0.0.0.0:#{port}")
             .with(protocol: Async::HTTP::Protocol::HTTP11)
end
