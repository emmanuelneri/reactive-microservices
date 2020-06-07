#!/usr/bin/env bash

mvn clean install -Dmaven.test.skip=true

mvn -f schedule-connector/pom.xml docker:build
mvn -f schedule-command/pom.xml docker:build
