FROM maven:3.6-jdk-8
COPY ./ ./
ENTRYPOINT mvn clean verify