package subredditdata;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;

import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

@Slf4j
public class ConsumerSubredditDataTopic {

    Logger logger;
    MessageHandlerSubredditData messageHandler;
    KafkaConsumer<String,String> consumer;

    public ConsumerSubredditDataTopic() {
        logger = LoggerFactory.getLogger(ConsumerSubredditDataTopic.class);
        String bootstrapServers = utils.Properties.getUrlKafka()+":9092";
        String grp_id = "consumer_app";
        List<String> topics = Collections.singletonList("subreddit-data");
        messageHandler = new MessageHandlerSubredditData();

        //Creating consumer properties
        Properties properties=new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, grp_id);
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        //creating consumer
        consumer= new KafkaConsumer<>(properties);

        //Subscribing
        consumer.subscribe(topics);
    }

    public void extractFromKafka() throws JSONException {
        System.out.println("##### CONSUMER ON TOPIC [SUBREDDIT-DATA] STARTED #####");

        Path filename = Path.of("stats-subredditdata.csv");

        // Create file header

        String finalRow = String.format("%s,%s,%s,%s,%s,%s",
                "messageType",
                "sentTime-millis",
                "receivedTime-millis",
                "receivedTime-nanos",
                "endConsumerProcessing-nanos",
                "endDbOperation-nanos");

        try {
            Files.writeString(filename,
                    finalRow + System.lineSeparator(),
                    CREATE,APPEND);
        } catch (Exception e) {
            e.printStackTrace();
        }


        //polling
        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
            records.forEach( record -> {
                //LocalDateTime receivedTime = LocalDateTime.now();
                long sentTime = record.timestamp();
                long receivedTimeMillis = System.currentTimeMillis();
                long receivedTimeNanos = System.nanoTime();
                try {
                    JSONObject message = new JSONObject(record.value());
                    message.put("timestamp",
                            LocalDateTime.ofInstant(Instant.ofEpochMilli(record.timestamp()),
                                    TimeZone.getDefault().toZoneId()).toString());
                    messageHandler.processMessage(record.key(), message, filename, sentTime, receivedTimeMillis, receivedTimeNanos);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            });
        }
    }
}