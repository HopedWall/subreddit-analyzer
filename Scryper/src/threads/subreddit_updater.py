from threading import Thread
import time
import json
from kafka import KafkaProducer
import os

class SubredditUpdater(Thread):
    def __init__(self, reddit, subreddit):
        Thread.__init__(self)
        self._reddit = reddit
        self._subreddit = subreddit
        self._dead = False
        self.url = os.getenv('KAFKA_CONTAINER', "localhost")
      
    def run(self):

        while not self._dead:
            subreddit_handle = self._reddit.subreddit(self._subreddit.get_name())

            # Push to kafka
            print("URL:"+self.url+':9092')

            producer = KafkaProducer(bootstrap_servers=self.url+':9092', key_serializer=lambda k: k.encode('utf-8'), value_serializer=lambda v: json.dumps(v).encode('utf-8'))
            

            ### ACTIVE USERS ###
             
            try:
                # NOTE: may not be necessary?
                # self._subreddit.update_active_users(subreddit_handle.accounts_active)
                
                active_users = dict()
                active_users['type'] = 'active_users' # maybe unnecessary
                active_users['value'] = subreddit_handle.accounts_active
                print(active_users)
                producer.send('subreddit-data', key='active_users', value=active_users)

            except:
                print("[WARN] This subreddit does not provide data about its active users")

            ################################################################################

            ### SUBSCRIBERS ###
            try:
                # NOTE: may not be necessary?
                #self._subreddit.update_visitors(subreddit_handle.subscribers)
                
                subscribers = dict()
                subscribers['type'] = 'subscribers' # maybe unnecessary
                subscribers['value'] = subreddit_handle.subscribers
                print(subscribers)
                producer.send('subreddit-data', key='subscribers', value=subscribers)
            except:
                print("[WARN] This subreddit does not provide data about its subscribers")
            
            time.sleep(60) # 5 minutes of sleep

    def stop(self):
        self._dead = True
        