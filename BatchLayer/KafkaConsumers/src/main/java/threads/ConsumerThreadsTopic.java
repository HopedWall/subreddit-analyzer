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
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

@Slf4j
public class ConsumerThreadsTopic {

    Logger logger;
    MessageHandlerThreads messageHandler;
    KafkaConsumer<String,String> consumer;

    public ConsumerThreadsTopic() throws IOException {
        logger = LoggerFactory.getLogger(ConsumerThreadsTopic.class);
        String bootstrapServers = utils.Properties.getUrlKafka()+":9092";
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

        Path filename = Path.of("stats-threads.csv");

        // Create file header

        String finalRow = String.format("%s,%s,%s,%s,%s",
                "messageType",
                "sentTime",
                "receivedTime",
                "endConsumerProcessing",
                "endDbOperation");

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

                long sentTime = record.timestamp();
                long receivedTime = System.currentTimeMillis();

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

                    messageHandler.processMessage(record.key(), message, filename, sentTime, receivedTime);
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }
}