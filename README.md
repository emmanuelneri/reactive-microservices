reactive-architecture
------------------------------------------------------
[![CircleCI](https://circleci.com/gh/emmanuelneri/reactive-microservices/tree/master.svg?style=svg&circle-token=c7c1c9ef3ae5b4148c847e3e554753fd456a6987)](<LINK>)
------------------------------------------------------


### schedule-connector
- Receving schedule request from HTTP Endpoint
- Validating request body (synchronous)
- Producing "ScheduleRequested" event to Kafka topic
- If body validation or Kafka sender fails return bad request
- If body validation and kafka sender ok return accepted http code and request id

### schedule-schema
- Define Schedule  structure

### schedule-command
- Receving schedule from Kafka
- Message consume 
  - Kafka Consumer with auto commit = false
  - Processing batch messages and commit only at the end
  - In case of invalid schema, offset will be committed and message will be sent to a DLQ
  - In case of unexpected error in any message, bateche message will not be commit 
- Consume only filled fields 
- Validate Schedule rules
 - If invalid business schedule, offset will be committed and message will be sent to a DLQ
- Persist schedule in Cassandra


TODO: 

- retornar processId
- Tolerante reader
- idempotente consumer
    - https://dzone.com/articles/kafka-clients-at-most-once-at-least-once-exactly-o
    - https://camel.apache.org/components/latest/eips/idempotentConsumer-eip.html
- Customer
- Join Schedule + Customer
- Criar Customer pelo Schedule
- Tópicos
    - Partições
    - Replicas (escalar Kafka)
- Producer mensagem repetidas ?
    - https://www.confluent.io/blog/exactly-once-semantics-are-possible-heres-how-apache-kafka-does-it/
    - Idempotent Producer
    - Deduplicate producer
- Fallback 
  - Connector : Kafka fora
  - Command : Cassandra fora
- Tolerância a falha (Review)
- Escalar consumers
- Tracing
