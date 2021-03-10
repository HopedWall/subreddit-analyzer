# Build phase.
FROM maven:3.5-jdk-11 as build
COPY ./ /speed-layer
WORKDIR /speed-layer
RUN mvn clean package -Dmaven.test.skip=true

# Run | Production phase.
FROM openjdk:8-jre-alpine
COPY --from=build /speed-layer/target/KafkaSubredditDataConsumer-0.0.1-SNAPSHOT.jar /app/kafka-consumer.jar
COPY --from=build /speed-layer/utility/stopwords-en.txt /app/utility/stopwords-en.txt
EXPOSE 8080
#ENV ELASTICSEARCH_CONTAINER=elastic-search
#ENV KAFKA_CONTAINER=kafka
WORKDIR /app
ENTRYPOINT ["java", "-jar", "kafka-consumer.jar"]