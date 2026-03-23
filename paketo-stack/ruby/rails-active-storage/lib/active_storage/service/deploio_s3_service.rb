require "active_storage/service/s3_service"

module ActiveStorage
  class Service
    class DeploioS3Service < ActiveStorage::Service::S3Service
      DEFAULT_REGION = "us-east-1" # fake; running in Switzerland, operated by Nine (see https://guides.deplo.io/ruby/active-storage.html#configure-active-storage)

      def initialize(host: nil, region: DEFAULT_REGION, **)
        @host = host
        super(region:, **)
      end

      def url(...)
        @host.blank? ? super : custom_host(super)
      end

      def url_for_direct_upload(...)
        @host.blank? ? super : custom_host(super)
      end

      private

      def custom_host(uri)
        uri = URI.parse(uri)
        uri.host = @host
        uri.to_s
      end
    end
  end
end
