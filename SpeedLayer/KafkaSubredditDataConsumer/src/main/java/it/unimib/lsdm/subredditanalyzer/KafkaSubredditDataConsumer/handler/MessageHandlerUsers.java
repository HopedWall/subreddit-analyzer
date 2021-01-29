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

    public void processMessage(String key, JSONObject message) throws JSONException, IOException {
        System.out.println("##### MESSAGE HANDLER #####");
        System.out.println("Type: " + message.get("type"));
        System.out.println("Message: " + message);

        // Eng: new user is cretaed in this analysis, if already exist it will be updated with current values (timestamp, upvotes etc.)
        // Ita: Non viene fatto il controllo se un utente esiste già, ma viene in ogni caso pushato, questo perhcè ci importa avere le info di un
        // certo periodo temporale, quindi se già presente il record viene aggiornato con tutti i valori attuali.
        if (message.get("type").equals("user-create")) {
            System.out.println("New user: " + key);
            IndexRequest indexRequest = new IndexRequest(index);

            // Removed and updated illegal fields (has name starts with "_")
            message.remove("type");
            message.remove("_id");
            message.put("upvotes", message.get("_upvotes"));
            message.remove("_upvotes");
            message.put("username", message.get("_username"));
            message.remove("_username");


            indexRequest.id(key);
            indexRequest.source(message.toString(), XContentType.JSON);
            System.out.println("Message: " + message.toString());
            IndexResponse response = client.index(indexRequest, RequestOptions.DEFAULT);

            System.out.println("RESPONSE status: " + response.status());
            System.out.println("RESPONSE status: " + response.getResult());
            /*} else {
                System.out.println("User already exist");
            }*/
        } else if (message.get("type").equals("user-update")) {
            GetRequest getRequest = new GetRequest(index);
            getRequest.id(key);
            GetResponse getResponse = client.get(getRequest, RequestOptions.DEFAULT);
            System.out.println("USER BEFORE UPDATE: " + getResponse.toString());

            UpdateRequest updateRequest = new UpdateRequest(index, key).doc("upvotes", message.get("upvotes"));
            UpdateResponse updateResponse = client.update(updateRequest, RequestOptions.DEFAULT);
            System.out.println("RESPONSE status: " + updateResponse.status());
            System.out.println("RESPONSE status: " + updateResponse.getResult());

            getResponse = client.get(getRequest, RequestOptions.DEFAULT);
            System.out.println("USER AFTER UPDATE: " + getResponse.toString());
        }

    }
}
