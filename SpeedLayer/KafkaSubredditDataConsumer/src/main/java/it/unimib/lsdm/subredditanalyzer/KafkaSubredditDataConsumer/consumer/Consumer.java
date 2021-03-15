package it.unimib.lsdm.subredditanalyzer.KafkaSubredditDataConsumer.consumer;

import it.unimib.lsdm.subredditanalyzer.KafkaSubredditDataConsumer.handler.MessageHandlerSubredditData;
import it.unimib.lsdm.subredditanalyzer.KafkaSubredditDataConsumer.handler.MessageHandlerThreadsData;
import it.unimib.lsdm.subredditanalyzer.KafkaSubredditDataConsumer.handler.MessageHandlerUsers;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.file.StandardOpenOption.CREATE;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.TimeZone;

@Slf4j
@Service
public class Consumer {

    @Autowired
    private MessageHandlerSubredditData messageHandlerSubredditdata;
    @Autowired
    private MessageHandlerUsers messageHandlerUsers;
    @Autowired
    private MessageHandlerThreadsData messageHandlerThreads;
    // Logger
    private final Logger logger = LoggerFactory.getLogger(Consumer.class);

    private final Path subredditDataPathFile;
    private final Path usersPathFile;
    private final Path threadsPathFile;

    public Consumer() throws IOException {
        subredditDataPathFile = Path.of("stats-subredditdata.csv");
        usersPathFile = Path.of("stats-users.csv");
        threadsPathFile = Path.of("stats-threads.csv");

        String headline = String.format("%s,%s,%s,%s,%s",
                "messageType",
                "sentTime",
                "receivedTime",
                "endConsumerProcessing",
                "endDbOperation");

        Files.writeString(subredditDataPathFile, headline + System.lineSeparator(), CREATE);
        Files.writeString(usersPathFile, headline + System.lineSeparator(), CREATE);
        Files.writeString(threadsPathFile, headline + System.lineSeparator(), CREATE);
    }

    @KafkaListener(topics = "subreddit-data", groupId = "group_id_subdata")
    public void consumeSubDataTopic(@Payload String message,
                        @Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key,
                        @Header(KafkaHeaders.RECEIVED_TIMESTAMP) long timestamp) throws IOException, JSONException {

        long receivedByConsumerTimestamp = System.currentTimeMillis();

        System.out.println("##### MESSAGE RECEIVED [SUBREDDIT-DATA] TOPIC #####");
        System.out.println("KEY: " + key);
        System.out.println("MESSAGE: " + message);
        System.out.println("TIMESTAMP: " + timestamp);

        JSONObject jsonMessage = new JSONObject(message);
        jsonMessage.append("timestamp",
                LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp),
                        TimeZone.getDefault().toZoneId())
        );

        // Handle message
        messageHandlerSubredditdata.processMessage(key, jsonMessage, subredditDataPathFile, timestamp, receivedByConsumerTimestamp);
    }

    @KafkaListener(topics = "users", groupId = "group_id_users")
    public void consumeUsersDataTopic(@Payload String message,
                        @Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key,
                        @Header(KafkaHeaders.RECEIVED_TIMESTAMP) long timestamp) throws JSONException, IOException {

        long receivedByConsumerTimestamp = System.currentTimeMillis();

        System.out.println("##### MESSAGE RECEIVED [USERS] TOPIC #####");
        System.out.println("KEY: " + key);
        System.out.println("MESSAGE: " + message);
        System.out.println("TIMESTAMP: " + timestamp);

        //Conversion of message inn json and append of LocalDateTime from kafka timestamp
        JSONObject jsonMessage = new JSONObject(message);
        jsonMessage.append("timestamp",
                LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp),
                        TimeZone.getDefault().toZoneId())
        );

        // Handle message
        messageHandlerUsers.processMessage(key, jsonMessage, usersPathFile, timestamp, receivedByConsumerTimestamp);
    }

    @KafkaListener(topics = "threads", groupId = "group_id_threads")
    public void consumeThreadsDataTopic(@Payload String message,
                                      @Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key,
                                      @Header(KafkaHeaders.RECEIVED_TIMESTAMP) long timestamp) throws JSONException, IOException {

        long receivedByConsumerTimestamp = System.currentTimeMillis();

        System.out.println("##### MESSAGE RECEIVED [THREADS] TOPIC #####");
        System.out.println("KEY: " + key);
        System.out.println("MESSAGE: " + message);
        System.out.println("TIMESTAMP: " + timestamp);

        //Conversion of message inn json and append of LocalDateTime from kafka timestamp
        JSONObject jsonMessage = new JSONObject(message);
        jsonMessage.append("timestamp",
                LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp),
                        TimeZone.getDefault().toZoneId())
        );

        // Handle message
        messageHandlerThreads.processMessage(key, jsonMessage, threadsPathFile, timestamp, receivedByConsumerTimestamp);
    }

}
