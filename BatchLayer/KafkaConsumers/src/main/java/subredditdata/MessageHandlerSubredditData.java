package subredditdata;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.json.JSONException;
import org.json.JSONObject;

public class MessageHandlerSubredditData {

    String connectionString = "mongodb://127.0.0.1:27017/";
    MongoClient mongoClient;
    MongoDatabase db;
    MongoCollection<Document> subredditDataCollection;


    public MessageHandlerSubredditData() {
        //System.out.println(connectionString);
        mongoClient = MongoClients.create(connectionString);
        db = mongoClient.getDatabase("reddit_data");
        subredditDataCollection = db.getCollection("subreddit_data_collection");
    }

    public void processMessage(String key, JSONObject message) throws JSONException {
        Document document = Document.parse(message.toString());
        System.out.println("Type: " + message.get("type"));

        System.out.println("INSERT: " + document);
        subredditDataCollection.insertOne(document);

/*        if (subredditDataCollection.countDocuments(Filters.eq("type", key)) == 0) {
            System.out.println("INSERT: " + document);
            subredditDataCollection.insertOne(document);
        } else {
            System.out.println("BEFORE UPDATE: " + subredditDataCollection.find(Filters.eq("type", key)).first());
            subredditDataCollection.findOneAndUpdate(Filters.eq("type", key), Updates.set("value", message.get("value")));
            System.out.println("AFTER UPDATE: " + subredditDataCollection.find(Filters.eq("type", key)).first());
        }*/
    }
}
