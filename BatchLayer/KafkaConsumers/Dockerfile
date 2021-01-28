# Build phase.
FROM maven:3.5-jdk-8 as build
COPY ./ /batch-layer
WORKDIR /batch-layer
RUN mvn clean package -Dmaven.test.skip=true

# Run | Production phase.
FROM openjdk:8-jre-alpine
COPY --from=build /batch-layer/target/KafkaConsumerSubredditDataTopic-1.0-SNAPSHOT.jar /app/kafka-consumer.jar
EXPOSE 8080
WORKDIR /app
ENTRYPOINT ["java", "-jar", "kafka-consumer.jar"]