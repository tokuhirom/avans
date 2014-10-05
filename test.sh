#!/bin/bash
set -e
set -x

mvn test

cd avans-setup
gem install bundler
bundle install
bundle exec rake test

