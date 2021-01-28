import threads.ConsumerThreadsTopic;

public class ThreadConsumerThreads extends Thread {
    Thread t;

    public void run() {
        try{
            ConsumerThreadsTopic c1 = new ConsumerThreadsTopic();
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
