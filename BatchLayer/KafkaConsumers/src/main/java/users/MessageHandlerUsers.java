package users;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.json.JSONException;
import org.json.JSONObject;

public class MessageHandlerUsers {

    String connectionString = "mongodb://127.0.0.1:27017/";
    MongoClient mongoClient;
    MongoDatabase db;
    MongoCollection<Document> usersCollection;


    public MessageHandlerUsers() {
        mongoClient = MongoClients.create(connectionString);
        db = mongoClient.getDatabase("reddit_data");
        usersCollection = db.getCollection("user_collection");
    }

    public void processMessage(String key, JSONObject message) throws JSONException {
        Document document = Document.parse(message.toString());
        System.out.println("Type: " + message.get("type"));

        switch (message.get("type").toString()) {
            case "user-create":
                //if (usersCollection.countDocuments(Filters.eq("_id", key)) == 0) {
                //    document.remove("type");
                    System.out.println("DocumentTOINSERT"+document.toString());
                    usersCollection.insertOne(document);
                //    System.out.println(document);
                //}
                break;
            case "user-update":
                //System.out.println(usersCollection.find(Filters.eq("_id", key)).first());

                //usersCollection.insertOne(document);

                //usersCollection.find(Filters.eq("id", key)).first();
                JSONObject old = new JSONObject(usersCollection.find(Filters.eq("id", key)).first());

                System.out.println("OLD BEFORE"+old);

                old.remove("_id");
                old.put("_upvotes", message.get("upvotes"));
                old.put("timestamp", message.get("timestamp"));

                Document doc = Document.parse(old.toString());

                usersCollection.insertOne(doc);
                System.out.println(old);
                //System.out.println(usersCollection.find(Filters.eq("_id", key)).first());
                break;
        }
    }
}

