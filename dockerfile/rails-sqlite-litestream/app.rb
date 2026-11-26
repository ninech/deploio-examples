# A Rails app in one file that keeps its data in SQLite although Deploio gives it no persistent disk.
# Litestream runs Puma and streams every change to S3 (see litestream.yml). Puma loads this file.
require "bundler/setup"
require "rails"
require "active_record/railtie"
require "action_controller/railtie"
require "action_view/railtie"

# The database file is lost with the container; Litestream restores it on start (see litestream.yml).
ENV["DATABASE_URL"] ||= "sqlite3:#{File.expand_path("storage/#{Rails.env}.sqlite3", __dir__)}"

class App < Rails::Application
  config.load_defaults 8.1
  config.root = __dir__
  config.eager_load = Rails.env.production?
  config.logger = ActiveSupport::TaggedLogging.logger($stdout) unless Rails.env.test?
  config.log_level = :info if Rails.env.production?
  config.action_controller.allow_forgery_protection = !Rails.env.test?

  routes.append do
    resources :notes, only: %i[create destroy]
    root "notes#index"
  end
end

App.initialize!

ActiveRecord::Schema.define do
  create_table :notes, if_not_exists: true do |t|
    t.text :body
    t.timestamps
  end
end

class Note < ActiveRecord::Base
  validates :body, presence: true
end

class NotesController < ActionController::Base
  def index
    @note = Note.new
    render_index
  end

  def create
    @note = Note.new(params.expect(note: [ :body ]))

    if @note.save
      redirect_to root_path
    else
      render_index status: :unprocessable_content
    end
  end

  def destroy
    Note.find(params.expect(:id)).destroy!
    redirect_to root_path, status: :see_other
  end

  private
    def render_index(status: :ok)
      render inline: <<~ERB, locals: { notes: Note.order(:created_at) }, status: status
        <!DOCTYPE html>
        <html>
          <head>
            <title>Notes</title>
            <meta name="viewport" content="width=device-width,initial-scale=1">
          </head>
          <body>
            <h1>Notes</h1>
            <ul id="notes">
              <% notes.each do |note| %>
                <li id="<%= dom_id note %>"><%= note.body %> <%= button_to "Delete", note, method: :delete, form: { style: "display: inline" } %></li>
              <% end %>
            </ul>
            <%= form_with model: @note do |form| %>
              <p style="color: red"><%= @note.errors.full_messages.to_sentence %></p>
              <%= form.textarea :body %>
              <%= form.submit %>
            <% end %>
            <footer>
              <p>Served by <%= Socket.gethostname %>. Redeploy the app: the notes stay.</p>
            </footer>
          </body>
        </html>
      ERB
    end
end
