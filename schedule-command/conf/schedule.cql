CREATE KEYSPACE IF NOT EXISTS dev WITH replication = {'class':'SimpleStrategy','replication_factor':'1'};

CREATE TABLE IF NOT EXISTS dev.schedule
(
    data_time       timestamp,
    description     text,
    document_number text,
    customer        text,
    phone           text,
    email           text,
    PRIMARY KEY (data_time, description, document_number)
);