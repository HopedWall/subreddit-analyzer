from threading import Thread
import time
import json
from kafka import KafkaProducer

class UserUpdater(Thread):
    def __init__(self, reddit, subreddit):
        Thread.__init__(self)
        self._reddit = reddit
        self._subreddit = subreddit
        self._dead = False
      
    def run(self):
        while not self._dead:
            subreddit_handle = self._reddit.subreddit(self._subreddit.get_name())
            
            data = dict()
            data['type'] = 'users'
            
            try:
                self._subreddit.update_active_users(subreddit_handle.accounts_active)
                data['active'] = subreddit_handle.accounts_active
            except:
                print("[WARN] This subreddit does not provide data about its active users")

            try:
                self._subreddit.update_visitors(subreddit_handle.subscribers)
                data['visitors'] = subreddit_handle.subscribers
            except:
                print("[WARN] This subreddit does not provide data about its subscribers")
            
            # Push to kafka
            producer = KafkaProducer(bootstrap_servers='localhost:9092', key_serializer=lambda k: k.encode('utf-8'), value_serializer=lambda v: json.dumps(v).encode('utf-8'))
            # TODO: create another channel
            producer.send('threads',key=str(self._subreddit.get_name()),value=data)
            
            time.sleep(5*60) # 5 minutes of sleep

    def stop(self):
        self._dead = True
        