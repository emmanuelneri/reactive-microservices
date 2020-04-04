#reactive-architecture
------------------------------------------------------
[![CircleCI](https://circleci.com/gh/emmanuelneri/reactive-microservices/tree/master.svg?style=svg&circle-token=c7c1c9ef3ae5b4148c847e3e554753fd456a6987)](<LINK>)
------------------------------------------------------


### schedule-connector
- Receving schedule request from HTTP Endpoint
- Validating request body (synchronous)
- Producing "ScheduleRequested" event to Kafka topic
- If body validation or Kafka sender fails return bad request
- If body validation and kafka sender ok return accepted http code and request id

TODO: 

- retornar processId
- Producer mensagem repetidas ?
    -  Deduplicate producer
- Tracing
- consumir scheduleSchema
- Tolerante reader
- idempotente consumer
- Tolerância a falha
  - offset?
  - DLQ?
  - retry? 
- Customer
- Join Schedule + Customer
- Criar Customer pelo Schedule
