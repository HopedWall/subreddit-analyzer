from threading import Thread
from entities.post import Post
from entities.comment import Comment
from entities.redditor import Redditor
import time
import json
from kafka import KafkaProducer
import os

class PostDownloader(Thread):
    def __init__(self, reddit, subreddit):
        Thread.__init__(self)
        self._reddit = reddit
        self._subreddit = subreddit
        self._dead = False
        # Fetching variable from ENV
        self.url = os.getenv('KAFKA_CONTAINER', "localhost")
      
    def run(self):
        while not self._dead:
            subreddit_handle = self._reddit.subreddit(self._subreddit.get_name())    
            
            print("URL:"+self.url+':9092')

            producer = KafkaProducer(bootstrap_servers=self.url+':9092', key_serializer=lambda k: k.encode('utf-8'), value_serializer=lambda v: json.dumps(v).encode('utf-8'))

            # Get the 10 hottest threads in the current subreddit
            # and push them to kafka if necessary
            for submission in subreddit_handle.hot(limit=10):

                ### POST ###
                # Store post locally
                post = Post(
                    submission.id,
                    submission.url, 
                    submission.author.name, 
                    submission.link_flair_text, 
                    str(submission.score), 
                    submission.title,
                    submission.selftext)

                # Check if post is already being tracked
                postPresent = post in self._subreddit.get_posts()

                if not postPresent:
                    # Add to tracked posts
                    self._subreddit.append_post(post)
                    # Send new post to Kafka
                    print(post.to_dict(postPresent))
                    producer.send('threads', key=str(post.get_id()), value=post.to_dict_new())

                else:
                    # Get last post update
                    lastPostUpdate = self._subreddit.get_post(post)
                    # Only push to kafka if upvotes have changed
                    postNotUpdated = post.get_upvotes() != lastPostUpdate.get_upvotes()
                    
                    if postNotUpdated: 
                        # Update local post
                        self._subreddit.update_post(lastPostUpdate, post)
                        # Send updated post to kafka
                        producer.send('threads', key=str(post.get_id()), value=post.to_dict_update())

                #################################################################################

                ### AUTHOR ###
                # Store the author of the post locally
                user = Redditor(
                    submission.author.id,
                    submission.author.name,
                    submission.score)

                userPresent = user in self._subreddit.get_users()

                if not userPresent:
                    # Add to tracked users
                    self._subreddit.add_user(user)
                    # Send new user to kafka
                    print(user.to_dict(userPresent))
                    producer.send('users', key=str(user.get_id()), value=user.to_dict_new())
                else:
                    # Get last user update
                    lastUserUpdate = self._subreddit.get_user(user)
                    # Only push to kafka if upvotes have changed
                    userNotUpdated = user.get_upvotes() != lastUserUpdate.get_upvotes()
                    
                    if userNotUpdated: 
                        # Update local user
                        self._subreddit.update_user(lastUserUpdate, user)
                        # Send updated user to kafka
                        print(user.to_dict(userPresent))
                        producer.send('users', key=str(user.get_id()), value=user.to_dict_update())

                time.sleep(1) # 2 second not to get kicked out of the API!
                
            time.sleep(60) # 60 seconds of sleep

    def stop(self):
        self._dead = True