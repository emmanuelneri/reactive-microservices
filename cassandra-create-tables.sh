#!/usr/bin/env bash

docker exec -it cassandra1 bash -c "cqlsh -f /tmp/schedule.cql"