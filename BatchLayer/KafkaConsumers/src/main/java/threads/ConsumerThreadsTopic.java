package threads;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;

@Slf4j
public class ConsumerThreadsTopic {

    Logger logger;
    MessageHandlerThreads messageHandler;
    KafkaConsumer<String,String> consumer;

    public ConsumerThreadsTopic() throws IOException {
        logger = LoggerFactory.getLogger(ConsumerThreadsTopic.class);
        String bootstrapServers = "127.0.0.1:9092";
        String grp_id = "consumer_app";
        List<String> topics = Collections.singletonList("threads");
        messageHandler = new MessageHandlerThreads();

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

        System.out.println("##### CONSUMER ON TOPIC [THREADS] STARTED #####");

        //polling
        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
            records.forEach( record -> {
                //logger.info("Key: " + record.key() + ", Value:" + record.value());
                //logger.info("Partition:" + record.partition() + ",Offset:" + record.offset());

                // Try to print json log.
                try {
                    JSONObject message = new JSONObject(record.value());

                    if (message.get("type").toString().equals("post-create")) {
                        String post_id = message.getString("_id");
                        message.remove("_id");
                        message.put("id", post_id);
                        System.out.println("MessageMODIFIED"+message);
                    }

                    if (message.get("type").toString().equals("comment-create")) {
                        String comment_id = message.getString("_id");
                        message.remove("_id");
                        message.put("id", comment_id);
                        System.out.println("MessageMODIFIED"+message);
                    }

                    message.put("timestamp",
                            LocalDateTime.ofInstant(Instant.ofEpochMilli(record.timestamp()),
                                    TimeZone.getDefault().toZoneId()).toString()
                    );
                    //logger.info("Key: " + message.get("_id"));
                    //logger.info("Type: " + message.get("type"));
                    messageHandler.processMessage(record.key(), message);
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }
}