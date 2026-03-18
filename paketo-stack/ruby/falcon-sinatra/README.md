# Deploio stages [sinatra](https://github.com/sinatra/sinatra) with [falcon](https://github.com/socketry/falcon)

## Deploy

To create a new app and deploy this project:

```shell
nctl create app falcon-sinatra-test2 \
     --git-url=https://github.com/ninech/deploio-examples \
     --git-sub-path=ruby/falcon-sinatra
```

## Development

To run locally:

```shell
bundle install
falcon serve
```
