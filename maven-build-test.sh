#!/usr/bin/env bash

mavenDirectory=/Users/emmanuelneri/.m2

docker build -t reactive-microservices/maven-build .

docker run \
    -v $mavenDirectory:/root/.m2 \
    -v /var/run/docker.sock:/var/run/docker.sock \
    -it reactive-microservices/maven-build