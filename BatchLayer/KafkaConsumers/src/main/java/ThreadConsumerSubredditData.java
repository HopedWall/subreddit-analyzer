import subredditdata.ConsumerSubredditDataTopic;

public class ThreadConsumerSubredditData extends Thread {
    Thread t;

    public void run() {
        ConsumerSubredditDataTopic c1 = new ConsumerSubredditDataTopic();
        try {
            c1.extractFromKafka();
        } catch (Exception e) {
        }
    }

    public void start () {
        if (t == null) {
            t = new Thread(this);
            t.start();
        }
    }
}
