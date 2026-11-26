ENV["RAILS_ENV"] = "test"
require_relative "../app"
require "rails/test_help"
require "minitest/autorun"

class NotesTest < ActionDispatch::IntegrationTest
  test "lists the notes and names the instance that served them" do
    Note.create!(body: "Buy milk")

    get root_url

    assert_response :success
    assert_select "h1", "Notes"
    assert_select "#notes li", /Buy milk/
    assert_select "footer", /Served by #{Socket.gethostname}/
  end

  test "creating a note stores it and goes back to the list" do
    assert_difference("Note.count") do
      post notes_url, params: { note: { body: "Call the plumber" } }
    end

    assert_redirected_to root_url
    assert_equal "Call the plumber", Note.last.body
  end

  test "a blank note is refused and the list is shown with the error" do
    assert_no_difference("Note.count") do
      post notes_url, params: { note: { body: "" } }
    end

    assert_response :unprocessable_content
    assert_select "form p", "Body can't be blank"
  end

  test "deleting a note removes it" do
    note = Note.create!(body: "Water the plants")

    assert_difference("Note.count", -1) do
      delete note_url(note)
    end

    assert_redirected_to root_url
  end
end
