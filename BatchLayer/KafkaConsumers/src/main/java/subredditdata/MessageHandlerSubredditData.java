package subredditdata;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.json.JSONException;
import org.json.JSONObject;
import utils.Properties;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

public class MessageHandlerSubredditData {

    String connectionString = "mongodb://"+Properties.getUrlMongo()+":27017/";
    MongoClient mongoClient;
    MongoDatabase db;
    MongoCollection<Document> subredditDataCollection;


    public MessageHandlerSubredditData() {
        mongoClient = MongoClients.create(connectionString);
        db = mongoClient.getDatabase("reddit_data");
        subredditDataCollection = db.getCollection("subreddit_data_collection");
    }

    public void processMessage(String key, JSONObject message, Path filename, long sentTime, long receivedTimeMillis, long receivedTimeNanos) throws JSONException {
        Document document = Document.parse(message.toString());
        String msgType = message.get("type").toString();
        System.out.println("Type: " + message.get("type"));
        System.out.println("INSERT: " + document);
        long endConsumerProcessing = System.nanoTime();
        subredditDataCollection.insertOne(document);
        long endDbOperation = System.nanoTime();

        try {

            String finalRow = String.format("%s,%d,%d,%d,%d,%d",
                                            msgType,
                                            sentTime,
                                            receivedTimeMillis,
                                            receivedTimeNanos,
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
