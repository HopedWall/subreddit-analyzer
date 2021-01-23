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

import java.util.ArrayList;
import java.util.List;

public class MessageHandler {

    String connectionString = "mongodb://127.0.0.1:27017/";
    MongoClient mongoClient;
    MongoDatabase db;
    MongoCollection<Document> postCollection;


    public MessageHandler() {
        //System.out.println(connectionString);
        mongoClient = MongoClients.create(connectionString);
        db = mongoClient.getDatabase("reddit_data");
        postCollection = db.getCollection("post_collection");
    }

    public void processMessage(String key, JSONObject message) throws JSONException {
        Document document = Document.parse(message.toString());
        System.out.println("Type: " + message.get("type"));

        switch (message.get("type").toString()) {
            case "post-create":
                if (postCollection.countDocuments(Filters.eq("_id", key)) == 0) {
                    // Removed text field --> not important.
                    document.remove("_text");
                    document.remove("type");
                    // Added comments field to add embedded comment related to post.
                    document.append("comments", new ArrayList<Document>());
                    System.out.println(document);
                    postCollection.insertOne(document);
                }
                break;
            case "comment-create":
                // Remove text from document
                // document.remove("_text");
                document.remove("type");
                // Get post with the key id of comment --> result with one record
                Document retrived = postCollection.find(Filters.eq("_id", key)).first();
                if (retrived != null) {
                    List<Document> embed = (List<Document>) retrived.get("comments");
                    System.out.println(retrived);
                    embed.add(document);
                    postCollection.findOneAndReplace(Filters.eq("_id", key), retrived);
                    System.out.println(retrived);
                }
                break;
            case "post-update":
                System.out.println(postCollection.find(Filters.eq("_id", key)).first());
                postCollection.findOneAndUpdate(Filters.eq("_id", key), Updates.set("_upvotes", message.get("upvotes")));
                System.out.println(postCollection.find(Filters.eq("_id", key)).first());
                break;
            case "comment-update":
                // Get comments in post
                if (postCollection.countDocuments(Filters.eq("_id", key)) > 0) {
                    Document post = postCollection.find(Filters.eq("_id", key)).first();
                    List<Document> embed = (List<Document>) post.get("comments");
                    // Note: used id and not the key, because the key represent id of related post (not of the comment)
                    embed.stream().filter(comment -> comment.get("_id").toString().equals(document.get("id").toString()))
                            .forEach(comment -> {
                                try {
                                    System.out.println("Upvotes before " + comment.get("_upvotes"));
                                    comment.replace("_upvotes", message.get("upvotes"));
                                    System.out.println("Upvotes after " + comment.get("_upvotes"));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            });
                    postCollection.findOneAndReplace(Filters.eq("_id", key), post);
                }
                break;
        }
    }
}
