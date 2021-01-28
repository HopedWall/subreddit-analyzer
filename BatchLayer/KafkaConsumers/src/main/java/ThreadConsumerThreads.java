import threads.ConsumerThreadsTopic;

public class ThreadConsumerThreads extends Thread {
    Thread t;

    public void run() {
        ConsumerThreadsTopic c1 = new ConsumerThreadsTopic();
        try{
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
