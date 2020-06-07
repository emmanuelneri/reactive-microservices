FROM openjdk:8-jdk-alpine
VOLUME /tmp
ADD target/schedule-connector-*.jar app.jar
ADD conf/schedule-connector.properties schedule-connector.properties
ENV ENVIRONMENT "DOCKER"
ENV KAFKA_BOOTSTRAP_SERVERS "kafka1:29092,kafka2:29093,kafka3:29094"
ENTRYPOINT [ "sh", "-c", "java -jar /app.jar" ]