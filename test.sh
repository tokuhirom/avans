#!/bin/bash
set -e
set -x

mvn install -DskipTests=true -Dgpg.skip=true

cd avans-setup
gem install bundler
bundle install
bundle exec rake test

