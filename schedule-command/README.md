## Schedule command

### Cassandra Query
1. Access Cassandra in Docker```docker exec -it cassandra1 bash```
2. Cassandra connect```cqlsh``` 
3. Execute Query```SELECT * from dev.schedule``` 
```
emmanuelneri@MacBook-Pro-de-Emmanuel  % docker exec -it cassandra bash                                         
root@ef9f1d97636a:/# cqlsh
Connected to Test Cluster at 127.0.0.1:9042.
[cqlsh 5.0.1 | Cassandra 3.11.6 | CQL spec 3.4.4 | Native protocol v4]
cqlsh> SELECT * from dev.schedule;

 data_time | description | document_number | customer | email | phone
-----------+-------------+-----------------+----------+-------+-------
```