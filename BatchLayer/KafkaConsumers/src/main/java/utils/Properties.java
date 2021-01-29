package utils;

public class Properties {
    private static String URL_KAFKA = System.getenv().getOrDefault("KAFKA_CONTAINER", "localhost");
    private static String URL_MONGO = System.getenv().getOrDefault("MONGO_CONTAINER", "localhost");

    public static String getUrlKafka() {
        return URL_KAFKA;
    }

    public static String getUrlMongo() {
        return URL_MONGO;
    }

}
