package it.unimib.lsdm.subredditanalyzer.KafkaSubredditDataConsumer.handler;

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

@Component
public class MessageHandlerSubredditData {
    private final RestHighLevelClient client;

    public MessageHandlerSubredditData() {
        ClientConfiguration clientConfiguration =
                ClientConfiguration.builder().connectedTo("localhost:9200").build();
        System.out.println(clientConfiguration);
        client = RestClients.create(clientConfiguration).rest();
    }

    public void processMessage(String key, JSONObject message) throws JSONException, IOException {
        System.out.println("##### MESSAGE HANDLER #####");
        System.out.println("Type: " + message.get("type"));
        System.out.println("Message: " + message);
        IndexRequest indexRequest = new IndexRequest("subreddit-data-json");
        indexRequest.source(message.toString(), XContentType.JSON);

        IndexResponse response = client.index(indexRequest, RequestOptions.DEFAULT);
        System.out.println("RESPONSE status: " + response.status());
    }
}


