from threading import Thread
from entities.post import Post
from entities.comment import Comment
from entities.redditor import Redditor
import time
import json
from kafka import KafkaProducer

class PostDownloader(Thread):
    def __init__(self, reddit, subreddit):
        Thread.__init__(self)
        self._reddit = reddit
        self._subreddit = subreddit
        self._dead = False
      
    def run(self):
        while not self._dead:
            subreddit_handle = self._reddit.subreddit(self._subreddit.get_name())    
            
            producer = KafkaProducer(bootstrap_servers='localhost:9092', key_serializer=lambda k: k.encode('utf-8'), value_serializer=lambda v: json.dumps(v).encode('utf-8'))

            # Get the 10 hottest threads in the current subreddit
            # and push them to kafka if necessary

            # Skip existing: for submission in sub.stream.submissions(skip_existing=True):
            #for submission in sub.stream.submissions(skip_existing=True):
            # WARN: skip_existing causes troubles!
            
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

                if not post in self._subreddit.get_posts():
                    # Add to tracked posts
                    self._subreddit.append_post(post)
                    
                    # Push to kafka
                    print(post.to_dict())
                    producer.send('threads',key=str(post.get_id()),value=post.to_dict())

                    ### AUTHOR ###
                    # This must be here since the user must be updated (locally) only if
                    # the post was not already inserted

                    # Store author locally
                    user = Redditor(
                        submission.author.id,
                        submission.author.name,
                        submission.score)

                    if not user in self._subreddit.get_users():
                        # Add to tracked users
                        self._subreddit.add_user(user)
                        
                        # Push to kafka
                        print(user.to_dict())
                        producer.send('users',key=str(user.get_id()),value=user.to_dict())
                    else:
                        # Get item from user list
                        index = self._subreddit.get_users().index(user)
                        old_user = self._subreddit.get_users()[index]
                        self._subreddit.get_users().remove(old_user)

                        # Add updated user
                        old_user.add_upvotes(user.get_upvotes())
                        self._subreddit.add_user(old_user)
                        
                        # Push to kafka
                        print("UPDATE:",old_user.to_dict())
                        producer.send('users',key=str(old_user.get_id()),value=old_user.to_dict())
                else:
                    print("Post already being tracked!")


                time.sleep(1) # 2 second not to get kicked out of the API!
                
            time.sleep(60) # 60 seconds of sleep

    def stop(self):
        self._dead = True