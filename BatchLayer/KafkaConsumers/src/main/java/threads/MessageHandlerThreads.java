package threads;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.vader.sentiment.analyzer.SentimentAnalyzer;
import edu.stanford.nlp.simple.*;
import org.bson.Document;
import org.json.JSONException;
import org.json.JSONObject;
import utils.Properties;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

public class MessageHandlerThreads {

    String connectionString = "mongodb://"+ Properties.getUrlMongo()+":27017/";
    MongoClient mongoClient;
    MongoDatabase db;
    MongoCollection<Document> postCollection;
    private Set<String> stopWords;


    public MessageHandlerThreads() throws IOException {
        //System.out.println(connectionString);
        mongoClient = MongoClients.create(connectionString);
        db = mongoClient.getDatabase("reddit_data");
        postCollection = db.getCollection("post_collection");
        stopWords = new LinkedHashSet<String>();
        BufferedReader SW= new BufferedReader(new FileReader("utility/stopwords-en.txt"));
        for(String line; (line = SW.readLine()) != null;)
            stopWords.add(line.trim());
        SW.close();
    }

    private String preprocessTextAndSentiment(String text) throws IOException {
        float globalCompound = 0;
        //Document doc = new Document(text);// Create document from text.

        edu.stanford.nlp.simple.Document doc = new edu.stanford.nlp.simple.Document(text);

        // 1) Split text in sentence (Splitting)
        for (Sentence sentence : doc.sentences()) {  // Will iterate over sentences in document
            //System.out.println("SENTENCE TO CLEAN: " + sentence);

            // 2) Remove stopwords (Tokenization + Cleaning)
            Stream<String> cleanedStream = sentence.words().stream().filter(word -> !stopWords.contains(word));
            String cleanedSentence = cleanedStream.collect(Collectors.joining(" "));

            //System.out.println("CLEANED SENTENCE: " + cleanedSentence);

            // 3) Sentiment for each sentence (Sentiment)
            SentimentAnalyzer sentimentAnalyzer = new SentimentAnalyzer(cleanedSentence);
            sentimentAnalyzer.analyze();

            //System.out.println("====> SENTIMENT: " + sentimentAnalyzer.getPolarity());

            // 4) Sum of compounds (Global sentiment)
            globalCompound += Float.parseFloat(sentimentAnalyzer.getPolarity().get("compound").toString());

            //System.out.println("############# END SENTENCE ##############");
        }
        System.out.println("TOTAL COMPOUND: " + globalCompound);
        return globalCompound >= 0.0 ? "pos" : "neg";
    }


    public void processMessage(String key, JSONObject message, Path filename, long sentTime, long receivedTime) throws JSONException, IOException {
        Document document = Document.parse(message.toString());
        System.out.println("Type: " + message.get("type"));

        long endConsumerProcessing = 0;
        long endDbOperation = 0;

        switch (message.get("type").toString()) {
            case "post-create":
                if (postCollection.countDocuments(Filters.eq("id", key)) == 0) {
                    // Added comments field to add embedded comment related to post.
                    document.append("comments", new ArrayList<Document>());

                    // Add sentiment
                    String polarity = preprocessTextAndSentiment(message.get("_text").toString());
                    document.put("polarity", polarity);

                    System.out.println(document);
                    endConsumerProcessing = System.currentTimeMillis();
                    postCollection.insertOne(document);
                    endDbOperation = System.currentTimeMillis();
                }
                break;
            case "comment-create":
                // Remove text from document

                // Add sentiment
                String polarity = preprocessTextAndSentiment(message.get("_text").toString());
                document.put("polarity", polarity);

                // Get post with the key id of comment --> result with one record
                Document retrieved = postCollection.find(Filters.eq("id", key)).first();
                if (retrieved != null) {
                    List<Document> embed = (List<Document>) retrieved.get("comments");
                    System.out.println(retrieved);
                    embed.add(document);
                    endConsumerProcessing = System.currentTimeMillis();
                    postCollection.findOneAndReplace(Filters.eq("id", key), retrieved);
                    endDbOperation = System.currentTimeMillis();
                    System.out.println(retrieved);
                }
                break;
            case "post-update":
                System.out.println(postCollection.find(Filters.eq("_id", key)).first());

                JSONObject old = new JSONObject(postCollection.find(Filters.eq("id", key)).first());

                old.remove("_id");
                old.put("_upvotes", message.get("upvotes"));
                old.put("timestamp", message.get("timestamp"));

                // Sentiment not necessary since should be the same

                Document doc = Document.parse(old.toString());

                endConsumerProcessing = System.currentTimeMillis();
                postCollection.insertOne(doc);
                endDbOperation = System.currentTimeMillis();

                System.out.println(doc);

                break;
            case "comment-update":
                // Get comments in post
                if (postCollection.countDocuments(Filters.eq("id", key)) > 0) {
                    Document post = postCollection.find(Filters.eq("id", key)).first();
                    List<Document> embed = (List<Document>) post.get("comments");
                    // Note: used id and not the key, because the key represent id of related post (not of the comment)
                    embed.stream().filter(comment -> comment.get("id").toString().equals(document.get("id").toString()))
                            .forEach(comment -> {
                                try {
                                    System.out.println("Upvotes before " + comment.get("_upvotes"));
                                    comment.replace("_upvotes", message.get("upvotes"));
                                    System.out.println("Upvotes after " + comment.get("_upvotes"));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            });

                    post.remove("_id");
                    endConsumerProcessing = System.currentTimeMillis();
                    postCollection.insertOne(post);
                    endDbOperation = System.currentTimeMillis();
                    //postCollection.findOneAndReplace(Filters.eq("id", key), post);
                }
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
