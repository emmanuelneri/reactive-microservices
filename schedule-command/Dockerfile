FROM openjdk:8-jdk-alpine
VOLUME /tmp
ADD target/schedule-command-*.jar app.jar
ADD conf/schedule-command.properties schedule-command.properties
ENV ENVIRONMENT "DOCKER"
ENV KAFKA_BOOTSTRAP_SERVERS "kafka1:29092,kafka2:29093,kafka3:29094"
ENV CASSANDRA_CONTACT_POINTS "cassandra1:9042,cassandra2:9043,cassandra3:9044"
ENTRYPOINT [ "sh", "-c", "java -jar /app.jar" ]