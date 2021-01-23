package utils;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.json.JSONException;
import org.json.JSONObject;

public class MessageHandler {

    String connectionString = "mongodb://127.0.0.1:27017/";
    MongoClient mongoClient;
    MongoDatabase db;
    MongoCollection<Document> usersCollection;


    public MessageHandler() {
        mongoClient = MongoClients.create(connectionString);
        db = mongoClient.getDatabase("reddit_data");
        usersCollection = db.getCollection("user_collection");
    }

    public void processMessage(String key, JSONObject message) throws JSONException {
        Document document = Document.parse(message.toString());
        System.out.println("Type: " + message.get("type"));

        switch (message.get("type").toString()) {
            case "user-create":
                if (usersCollection.countDocuments(Filters.eq("_id", key)) == 0) {
                    document.remove("type");
                    usersCollection.insertOne(document);
                    System.out.println(document);
                }
                break;
            case "user-update":
                System.out.println(usersCollection.find(Filters.eq("_id", key)).first());
                usersCollection.findOneAndUpdate(Filters.eq("_id", key), Updates.set("_upvotes", message.get("upvotes")));
                System.out.println(usersCollection.find(Filters.eq("_id", key)).first());
                break;
        }
    }
}

