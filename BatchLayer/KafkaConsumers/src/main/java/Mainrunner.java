public class Mainrunner extends Thread {
    public static void main(String args[]) {
        ThreadConsumerSubredditData T1 = new ThreadConsumerSubredditData();
        T1.start();

        ThreadConsumerThreads T2 = new ThreadConsumerThreads();
        T2.start();

        ThreadConsumerUsers T3 = new ThreadConsumerUsers();
        T3.start();
    }
}


