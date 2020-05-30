reactive-architecture
------------------------------------------------------
[![CircleCI](https://circleci.com/gh/emmanuelneri/reactive-microservices/tree/master.svg?style=svg&circle-token=c7c1c9ef3ae5b4148c847e3e554753fd456a6987)](<LINK>)

## Architecture

![alt tag](https://github.com/emmanuelneri/reactive-microservices/blob/master/architecture.jpeg?style=centerme)

### schedule-connector
- Receive schedule request from HTTP Endpoint
- Validate request body and require fields before response
    - If the validation or Kafka sender fails return bad request
    - If the validation and kafka sender ok return accepted http code
- Endpoint response a request Id to get processed request back
- Produce "ScheduleRequested" event to Kafka topic

### schedule-schema
- Define Schedule structure

### schedule-command
- Consume "ScheduleRequested" event from Kafka
  - Kafka Consumer with auto commit = false
  - Only filled fields will be considered
  - Processing batch messages and commit only at the end
  - If the invalid schema: offset will be committed and message will be sent to a DLQ (TODO)
  - If the schedule has invalid business, the offset will be committed and message will be sent to a DLQ (TODO)
  - If it happens any unexpected error, the batch messages will not be commit 
  - If it happens unexpected error in last message, the previous messages could be processed
- All schedule are persisted in Cassandra
  - Schedule and Customer persist in a single table
  - Table key is composed by dateTime, description and customer Document number 
- After processed produce schedule event
  - ScheduleProcessed is produced with requestId 
  - Schedule change topic is produced

## Running environment 

1. Start infrastructure (Kafka, Cassandra), execute ```docker-compose up```
3. Create topics ```./kafka-create-topics.sh```
2. Create tables ```./cassandra-create-tables.sh```


## TODO: 

- Tolerante reader
- idempotente consumer
    - https://dzone.com/articles/kafka-clients-at-most-once-at-least-once-exactly-o
    - https://camel.apache.org/components/latest/eips/idempotentConsumer-eip.html
- Teste do cenário
- Fallback 
  - Connector : Kafka fora
  - Command : Cassandra fora
- Tolerância a falha (Review)
- Escalar consumers
- ReprocessId: Retornar apenas um em caso de falha
