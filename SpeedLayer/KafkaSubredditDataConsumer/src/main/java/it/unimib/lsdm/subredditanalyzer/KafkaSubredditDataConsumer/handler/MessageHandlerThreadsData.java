package it.unimib.lsdm.subredditanalyzer.KafkaSubredditDataConsumer.handler;

import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
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
public class MessageHandlerThreadsData {
    private final RestHighLevelClient client;
    private final String index = "threads-data-json";

    public MessageHandlerThreadsData() throws IOException {
        ClientConfiguration clientConfiguration =
                ClientConfiguration.builder().connectedTo("localhost:9200").build();
        System.out.println(clientConfiguration);
        client = RestClients.create(clientConfiguration).rest();
    }

    public void processMessage(String key, JSONObject message) throws JSONException, IOException {
        GetRequest getRequest = new GetRequest(index);
        GetResponse getResponse;
        System.out.println("##### MESSAGE HANDLER #####");
        System.out.println("Type: " + message.get("type"));
        System.out.println("Message: " + message);

        switch (message.get("type").toString()) {
            case "post-create":
                // Removed fields
                message.remove("_text");
                message.remove("type");
                message.remove("_id");

                // Renamed fields
                message.put("url", message.get("_url"));
                message.remove("_url");
                message.put("author", message.get("_author"));
                message.remove("_author");
                message.put("flairs", message.get("_flairs"));
                message.remove("_flairs");
                message.put("upvotes", Integer.parseInt(message.get("_upvotes").toString()));
                message.remove("_upvotes");
                message.put("title", message.get("_title"));
                message.remove("_title");

                // Added new field for comment count
                message.put("comment_num", 0);

                System.out.println("New post: " + key);

                IndexRequest indexRequest = new IndexRequest(index);
                indexRequest.id(key);
                indexRequest.source(message.toString(), XContentType.JSON);

                System.out.println("Message: " + message.toString());

                IndexResponse response = client.index(indexRequest, RequestOptions.DEFAULT);

                System.out.println("RESPONSE status: " + response.status());
                System.out.println("RESPONSE status: " + response.getResult());
                break;
            case "comment-create":
                getRequest.id(key);
                getResponse = client.get(getRequest, RequestOptions.DEFAULT);
                System.out.println("POST BEFORE #COMMENT UPDATE: " + getResponse.toString());

                if (getResponse.isExists()) {
                    int comment_num = Integer.parseInt(getResponse.getSource().get("comment_num").toString()) + 1;
                    // Update existing post.
                    UpdateRequest updateRequest = new UpdateRequest(index, key).doc("comment_num", comment_num);
                    UpdateResponse updateResponse = client.update(updateRequest, RequestOptions.DEFAULT);
                    System.out.println("RESPONSE status: " + updateResponse.status());
                    System.out.println("RESPONSE status: " + updateResponse.getResult());

                    // Get updated posy.
                    getResponse = client.get(getRequest, RequestOptions.DEFAULT);
                    System.out.println("POST AFTER #COMMENT UPDATE: " + getResponse.toString());
                } else {
                    System.out.println("POST DOESN'T EXIST");
                }
                break;
            case "post-update":
                // Get existing post.
                getRequest.id(key);
                getResponse = client.get(getRequest, RequestOptions.DEFAULT);
                System.out.println("POST BEFORE UPDATE: " + getResponse.toString());

                if (getResponse.isExists()) {
                    // Update existing post.
                    UpdateRequest updateRequest = new UpdateRequest(index, key).doc("upvotes", Integer.parseInt(message.get("upvotes").toString()));
                    UpdateResponse updateResponse = client.update(updateRequest, RequestOptions.DEFAULT);
                    System.out.println("RESPONSE status: " + updateResponse.status());
                    System.out.println("RESPONSE status: " + updateResponse.getResult());

                    // Get updated posy.
                    getResponse = client.get(getRequest, RequestOptions.DEFAULT);
                    System.out.println("POST AFTER UPDATE: " + getResponse.toString());
                } else {
                    System.out.println("POST DOESN'T EXIST");
                }
                break;
            case "comment-update":
                System.out.println("-------------- SKIPPED EVENT --------------");
                break;
        }

    }
}
