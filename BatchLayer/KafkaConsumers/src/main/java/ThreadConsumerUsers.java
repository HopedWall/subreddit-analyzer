import users.UsersTopicConsumer;

public class ThreadConsumerUsers extends Thread {
    Thread t;

    public void run() {
        UsersTopicConsumer c1 = new UsersTopicConsumer();
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
