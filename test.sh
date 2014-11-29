#!/bin/bash
set -e
set -x

mvn install -DskipTests=true -Dgpg.skip=true
mvn javadoc:javadoc

cd avans-setup
./bin/avans_setup com.example.foo

