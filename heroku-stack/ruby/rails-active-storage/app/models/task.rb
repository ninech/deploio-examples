class Task < ApplicationRecord
  has_one_attached :attachment
  has_one_attached :direct_upload_attachment
end
