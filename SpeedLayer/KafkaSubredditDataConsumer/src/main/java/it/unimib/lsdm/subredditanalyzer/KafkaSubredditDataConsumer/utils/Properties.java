package it.unimib.lsdm.subredditanalyzer.KafkaSubredditDataConsumer.utils;

public class Properties {
    private static String ELASTIC_URL = System.getenv().getOrDefault("ELASTICSEARCH_CONTAINER", "localhost");

    public static String getElasticUrl() {
        return ELASTIC_URL;
    }
}
