require "rails_helper"
require "aws-sdk-s3"
require "active_storage/service/deploio_s3_service"

RSpec.describe "ActiveStorage::Service::DeploioS3Service" do
  subject(:service) do
    ActiveStorage::Service.configure(:deploio_s3_test, active_storage_config)
  end

  before do
    allow(Aws::S3::Resource).to receive(:new).and_return(Aws::S3::Resource.new(region: "us-east-1",
                                                                               stub_responses: true))
  end

  describe "#url" do
    subject(:url) do
      service.url("test", expires_in: 5.minutes, filename: ActiveStorage::Filename.new("test.txt"),
                  disposition: :inline, content_type: "text/plain")
    end

    context "when host is blank" do
      let(:active_storage_config) do
        ActiveSupport::ConfigurationFile.parse(Rails.root.join("spec/support/active_storage/s3_configuration_with_missing_host.yml"),
                                               symbolize_names: true)
      end

      it "returns a URL without a custom host" do
        expect(url).to start_with("https://s3.amazonaws.com/test")
      end
    end

    context "when host is NOT blank" do
      context "when bucket is NOT public" do
        let(:active_storage_config) do
          ActiveSupport::ConfigurationFile.parse(Rails.root.join("spec/support/active_storage/s3_configuration_private.yml"),
                                                 symbolize_names: true)
        end

        it "returns a signed URL with the configured host" do
          expect(url).to start_with("https://example.com/test")
          expect(url).to include("X-Amz-Signature=")
        end
      end

      context "when bucket is public" do
        let(:active_storage_config) do
          ActiveSupport::ConfigurationFile.parse(Rails.root.join("spec/support/active_storage/s3_configuration_public.yml"),
                                                 symbolize_names: true)
        end

        it "returns a URL with the configured host" do
          expect(url).to eq("https://example.com/test")
        end
      end
    end
  end

  describe "#url_for_direct_upload" do
    subject(:url_for_direct_upload) do
      service.url_for_direct_upload("test", expires_in: 5.minutes,
                                    content_type: "text/plain", content_length: 1234,
                                    checksum: "d41d8cd98f00b204e9800998ecf8427e")
    end

    context "when host is blank" do
      let(:active_storage_config) do
        ActiveSupport::ConfigurationFile.parse(Rails.root.join("spec/support/active_storage/s3_configuration_with_missing_host.yml"),
                                               symbolize_names: true)
      end

      it "returns a URL without a custom host" do
        expect(url_for_direct_upload).to start_with("https://s3.amazonaws.com/test")
      end
    end

    context "when host is NOT blank" do
      context "when bucket is NOT public" do
        let(:active_storage_config) do
          ActiveSupport::ConfigurationFile.parse(Rails.root.join("spec/support/active_storage/s3_configuration_private.yml"),
                                                 symbolize_names: true)
        end

        it "returns a signed URL with the configured host" do
          expect(url_for_direct_upload).to start_with("https://example.com/test")
          expect(url_for_direct_upload).to include("X-Amz-Signature=")
        end
      end

      context "when bucket is public" do
        let(:active_storage_config) do
          ActiveSupport::ConfigurationFile.parse(Rails.root.join("spec/support/active_storage/s3_configuration_public.yml"),
                                                 symbolize_names: true)
        end

        it "returns a URL with the configured host" do
          expect(url_for_direct_upload).to start_with("https://example.com/test?x-amz-acl=public-read")
        end
      end
    end
  end
end
