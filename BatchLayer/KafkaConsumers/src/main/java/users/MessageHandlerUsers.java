package users;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.json.JSONException;
import org.json.JSONObject;
import utils.Properties;

import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

public class MessageHandlerUsers {

    String connectionString = "mongodb://"+ Properties.getUrlMongo()+":27017/";
    MongoClient mongoClient;
    MongoDatabase db;
    MongoCollection<Document> usersCollection;


    public MessageHandlerUsers() {
        mongoClient = MongoClients.create(connectionString);
        db = mongoClient.getDatabase("reddit_data");
        usersCollection = db.getCollection("user_collection");
    }

    public void processMessage(String key, JSONObject message, Path filename, long sentTime, long receivedTime) throws JSONException {
        Document document = Document.parse(message.toString());
        System.out.println("Type: " + message.get("type"));

        long endConsumerProcessing = 0;
        long endDbOperation = 0;

        switch (message.get("type").toString()) {
            case "user-create":
                    System.out.println("DocumentTOINSERT"+document.toString());
                    endConsumerProcessing = System.currentTimeMillis();
                    usersCollection.insertOne(document);
                    endDbOperation = System.currentTimeMillis();
                break;
            case "user-update":
                JSONObject old = new JSONObject(usersCollection.find(Filters.eq("id", key)).first());
                System.out.println("OLD BEFORE"+old);

                old.remove("_id");
                old.put("_upvotes", message.get("upvotes"));
                old.put("timestamp", message.get("timestamp"));

                Document doc = Document.parse(old.toString());

                endConsumerProcessing = System.currentTimeMillis();
                usersCollection.insertOne(doc);
                endDbOperation = System.currentTimeMillis();
                System.out.println(old);
                //System.out.println(usersCollection.find(Filters.eq("_id", key)).first());
                break;
        }

        try {
                String finalRow = String.format("%d,%d,%d,%d",
                        sentTime,
                        receivedTime,
                        endConsumerProcessing,
                        endDbOperation);

                Files.writeString(filename,
                        finalRow + System.lineSeparator(),
                        CREATE,APPEND);

            } catch (Exception e) {
                e.printStackTrace();
            }

    }
}

