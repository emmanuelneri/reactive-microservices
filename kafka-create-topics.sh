#!/usr/bin/env bash

docker-compose exec kafka1 kafka-topics --create --topic ScheduleReceived --partitions 2 --replication-factor 3 --if-not-exists --zookeeper zookeeper:2181
docker-compose exec kafka1 kafka-topics --create --topic ScheduleProcessed --partitions 2 --replication-factor 3 --if-not-exists --zookeeper zookeeper:2181
docker-compose exec kafka1 kafka-topics --create --topic ScheduleInvalid --partitions 2 --replication-factor 3 --if-not-exists --zookeeper zookeeper:2181

echo "-------describe Topics------"
docker-compose exec kafka1 kafka-topics --describe --zookeeper zookeeper:2181
echo "-----------------------------"