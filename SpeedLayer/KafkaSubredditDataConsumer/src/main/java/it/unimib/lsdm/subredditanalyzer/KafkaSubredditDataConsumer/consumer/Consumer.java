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

    @KafkaListener(topics = "subreddit-data", groupId = "group_id")
    public void consumeSubDataTopic(@Payload String message,
                        @Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key,
                        @Header(KafkaHeaders.RECEIVED_TIMESTAMP) long timestamp) throws IOException, JSONException {

        System.out.println("##### MESSAGE RECEIVED [SUBREDDIT-DATA] TOPIC #####");
        System.out.println("KEY: " + key);
        System.out.println("MESSAGE: " + message);
        System.out.println("TIMESTAMP: " + timestamp);
        logger.info(String.format("#### -> Consumed message -> %s", message));

        JSONObject jsonMessage = new JSONObject(message);
        jsonMessage.append("timestamp",
                LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp),
                        TimeZone.getDefault().toZoneId())
        );

        logger.info(String.format("After handle message -> %s", jsonMessage));

        // Handle message
        messageHandlerSubredditdata.processMessage(key, jsonMessage);
    }

    @KafkaListener(topics = "users", groupId = "group_id_users")
    public void consumeUsersDataTopic(@Payload String message,
                        @Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key,
                        @Header(KafkaHeaders.RECEIVED_TIMESTAMP) long timestamp) throws JSONException, IOException {
        System.out.println("##### MESSAGE RECEIVED [USERS] TOPIC #####");
        System.out.println("KEY: " + key);
        System.out.println("MESSAGE: " + message);
        System.out.println("TIMESTAMP: " + timestamp);
        logger.info(String.format("#### -> Consumed message -> %s", message));

        //Conversion of message inn json and append of LocalDateTime from kafka timestamp
        JSONObject jsonMessage = new JSONObject(message);
        jsonMessage.append("timestamp",
                LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp),
                        TimeZone.getDefault().toZoneId())
        );
        logger.info(String.format("After handle message -> %s", jsonMessage));

        // Handle message
        messageHandlerUsers.processMessage(key, jsonMessage);
    }

    @KafkaListener(topics = "threads", groupId = "group_id_users")
    public void consumeThreadsDataTopic(@Payload String message,
                                      @Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key,
                                      @Header(KafkaHeaders.RECEIVED_TIMESTAMP) long timestamp) throws JSONException, IOException {
        System.out.println("##### MESSAGE RECEIVED [THREADS] TOPIC #####");
        System.out.println("KEY: " + key);
        System.out.println("MESSAGE: " + message);
        System.out.println("TIMESTAMP: " + timestamp);
        logger.info(String.format("#### -> Consumed message -> %s", message));

        //Conversion of message inn json and append of LocalDateTime from kafka timestamp
        JSONObject jsonMessage = new JSONObject(message);
        jsonMessage.append("timestamp",
                LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp),
                        TimeZone.getDefault().toZoneId())
        );
        logger.info(String.format("After handle message -> %s", jsonMessage));

        // Handle message
        messageHandlerThreads.processMessage(key, jsonMessage);
    }

}
