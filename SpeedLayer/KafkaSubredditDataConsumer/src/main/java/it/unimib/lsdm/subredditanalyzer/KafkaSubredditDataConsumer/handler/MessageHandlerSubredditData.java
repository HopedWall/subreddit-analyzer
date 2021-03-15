package it.unimib.lsdm.subredditanalyzer.KafkaSubredditDataConsumer.handler;

import it.unimib.lsdm.subredditanalyzer.KafkaSubredditDataConsumer.utils.Properties;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.RestClients;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.file.StandardOpenOption.APPEND;

@Component
public class MessageHandlerSubredditData {
    private final RestHighLevelClient client;

    public MessageHandlerSubredditData() {
        String url = Properties.getElasticUrl() + ":9200";
        ClientConfiguration clientConfiguration =
                ClientConfiguration.builder().connectedTo(url).build();
        System.out.println(clientConfiguration);
        client = RestClients.create(clientConfiguration).rest();
    }

    public void processMessage(String key, JSONObject message, Path subredditDataPath,
                               long receivedByKafkaTimestamp, long receivedByConsumerTimestamp) throws JSONException, IOException {
        String msgType = message.get("type").toString();

        System.out.println("##### MESSAGE HANDLER #####");
        System.out.println("Type: " + msgType);
        System.out.println("Message: " + message);

        IndexRequest indexRequest = new IndexRequest("subreddit-data-json");
        indexRequest.source(message.toString(), XContentType.JSON);

        long endConsumerProcessingTimestamp = System.currentTimeMillis();
        IndexResponse response = client.index(indexRequest, RequestOptions.DEFAULT);
        long endDbOperationTimestamp = System.currentTimeMillis();

        System.out.println("RESPONSE status: " + response.status());

        Files.writeString(subredditDataPath,
                String.format("%s,%d,%d,%d,%d",
                        msgType,
                        receivedByKafkaTimestamp,
                        receivedByConsumerTimestamp,
                        endConsumerProcessingTimestamp,
                        endDbOperationTimestamp) + System.lineSeparator(),
                APPEND);
    }
}


