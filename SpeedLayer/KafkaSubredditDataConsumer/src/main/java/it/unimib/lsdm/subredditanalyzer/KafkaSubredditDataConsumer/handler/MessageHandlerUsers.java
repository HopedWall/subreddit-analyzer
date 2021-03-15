package it.unimib.lsdm.subredditanalyzer.KafkaSubredditDataConsumer.handler;

import it.unimib.lsdm.subredditanalyzer.KafkaSubredditDataConsumer.utils.Properties;
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
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.file.StandardOpenOption.APPEND;

@Component
public class MessageHandlerUsers {
    private final RestHighLevelClient client;
    private final String index = "users-data-json";

    public MessageHandlerUsers() throws IOException {
        String url = Properties.getElasticUrl() + ":9200";
        ClientConfiguration clientConfiguration =
                ClientConfiguration.builder().connectedTo(url).build();
        System.out.println(clientConfiguration);
        client = RestClients.create(clientConfiguration).rest();
    }

    public void processMessage(String key, JSONObject message, Path userPathfile,
                               long receivedByKafkaTimestamp,
                               long receivedByConsumerTimestampMillis,
                               long receivedByConsumerTimestampNanos) throws JSONException, IOException {
        String msgType = message.get("type").toString();

        System.out.println("##### MESSAGE HANDLER #####");
        System.out.println("Type: " + msgType);
        System.out.println("Message: " + message);

        long endConsumerProcessingTimestamp = 0, endDbOperationTimestamp = 0;

        // Eng: new user is cretaed in this analysis, if already exist it will be updated with current values (timestamp, upvotes etc.)
        if (message.get("type").equals("user-create")) {
            IndexRequest indexRequest = new IndexRequest(index);

            // Removed and updated illegal fields (its name starts with "_")
            message.remove("type");
            message.remove("_id");
            message.put("upvotes", Integer.parseInt(message.get("_upvotes").toString()));
            message.remove("_upvotes");
            message.put("username", message.get("_username"));
            message.remove("_username");

            indexRequest.id(key);
            indexRequest.source(message.toString(), XContentType.JSON);

            endConsumerProcessingTimestamp = System.nanoTime();
            IndexResponse response = client.index(indexRequest, RequestOptions.DEFAULT);
            endDbOperationTimestamp = System.nanoTime();

            System.out.println("RESPONSE status: " + response.getResult() + "-" + response.status());
        } else if (message.get("type").equals("user-update")) {
            /*GetRequest getRequest = new GetRequest(index);
            getRequest.id(key);
            GetResponse getResponse = client.get(getRequest, RequestOptions.DEFAULT);
            System.out.println("USER BEFORE UPDATE: " + getResponse.toString());*/

            UpdateRequest updateRequest = new UpdateRequest(index, key).doc("upvotes", Integer.parseInt(message.get("upvotes").toString()));

            endConsumerProcessingTimestamp = System.nanoTime();
            UpdateResponse updateResponse = client.update(updateRequest, RequestOptions.DEFAULT);
            endDbOperationTimestamp = System.nanoTime();

            System.out.println("RESPONSE status: " + updateResponse.getResult() + "-" + updateResponse.status());

            /*getResponse = client.get(getRequest, RequestOptions.DEFAULT);
            System.out.println("USER AFTER UPDATE: " + getResponse.toString());*/
        }

        Files.writeString(userPathfile,
                String.format("%s,%d,%d,%d,%d,%d",
                        msgType,
                        receivedByKafkaTimestamp,
                        receivedByConsumerTimestampMillis,
                        receivedByConsumerTimestampNanos,
                        endConsumerProcessingTimestamp,
                        endDbOperationTimestamp) + System.lineSeparator(),
                APPEND);
    }
}
