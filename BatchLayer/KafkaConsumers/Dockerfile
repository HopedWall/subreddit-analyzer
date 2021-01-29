# Build phase.
FROM maven:3.5-jdk-11 as build
COPY ./ /batch-layer
WORKDIR /batch-layer
RUN mvn clean compile assembly:single

# Run | Production phase.
FROM openjdk:11
COPY --from=build /batch-layer/target/KafkaConsumerSubredditDataTopic-1.0-SNAPSHOT-jar-with-dependencies.jar /app/kafka-consumer.jar
COPY --from=build /batch-layer/utility/stopwords-en.txt /app/utility/stopwords-en.txt
WORKDIR /app
#ENV KAFKA_CONTAINER=kafka
#ENV MONGO_CONTAINER=mongo
ENTRYPOINT ["java", "-jar", "kafka-consumer.jar"]