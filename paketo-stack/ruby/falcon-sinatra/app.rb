require "sinatra/base"

class App < Sinatra::Base
  get "/" do
    "Hello Falcon!"
  end
end
