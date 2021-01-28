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

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;

@Slf4j
public class ConsumerSubredditDataTopic {

    Logger logger;
    MessageHandlerSubredditData messageHandler;
    KafkaConsumer<String,String> consumer;

    public ConsumerSubredditDataTopic() {
        logger = LoggerFactory.getLogger(ConsumerSubredditDataTopic.class);
        String bootstrapServers = "127.0.0.1:9092";
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

        //polling
        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
            records.forEach( record -> {
                //logger.info("Key: " + record.key() + ", Value:" + record.value());
                //logger.info("Partition:" + record.partition() + ",Offset:" + record.offset());

                // Try to print json log.
                try {
                    JSONObject message = new JSONObject(record.value());
                    message.put("timestamp",
                            LocalDateTime.ofInstant(Instant.ofEpochMilli(record.timestamp()),
                                    TimeZone.getDefault().toZoneId()).toString());
                    //logger.info("Key: " + message.get("_id"));
                    //logger.info("Type: " + message.get("type"));
                    messageHandler.processMessage(record.key(), message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            });
        }
    }
}