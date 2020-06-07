#!/usr/bin/env bash

docker run -d \
    --name schedule-connector \
    --network=reactive-microservices \
    -p 8080:8080 \
    reactive-microservices/schedule-connector

docker run -d \
    --name schedule-command \
    --network=reactive-microservices \
    reactive-microservices/schedule-command