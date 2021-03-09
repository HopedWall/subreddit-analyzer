import users.ConsumerUsersTopic;

public class ThreadConsumerUsers extends Thread {
    Thread t;

    public void run() {
        ConsumerUsersTopic c1 = new ConsumerUsersTopic();
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
