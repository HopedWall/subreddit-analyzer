import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.json.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.MessageHandler;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

@Slf4j
public class UsersTopicConsumer {
    public static void main(String[] args) throws JSONException {
        Logger logger = LoggerFactory.getLogger(UsersTopicConsumer.class);
        String bootstrapServers = "127.0.0.1:9092";
        String grp_id = "consumer_app";
        List<String> topics = Collections.singletonList("users");
        MessageHandler messageHandler = new MessageHandler();

        //Creating consumer properties  
        Properties properties=new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, grp_id);
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        //creating consumer
        KafkaConsumer<String,String> consumer= new KafkaConsumer<>(properties);

        //Subscribing
        consumer.subscribe(topics);

        System.out.println("##### CONSUMER ON TOPIC [USERS] STARTED #####");

        //polling
        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
            records.forEach( record -> {
                //logger.info("Key: " + record.key() + ", Value:" + record.value());
                //logger.info("Partition:" + record.partition() + ",Offset:" + record.offset());

                // Try to print json log.
                try {
                    JSONObject message = new JSONObject(record.value());
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