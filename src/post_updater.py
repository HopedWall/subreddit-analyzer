from threading import Thread
from comment import Comment
import time
import json
from kafka import KafkaProducer

class VotesUpdater(Thread):
    def __init__(self, reddit, subreddit):
        Thread.__init__(self)
        self._reddit = reddit
        self._subreddit = subreddit
        self._dead = False
      
    def run(self):
        while not self._dead:

            # Get a copy of posts list 
            posts = self._subreddit.get_posts()
            
            for post in posts:
                
                submission = self._reddit.submission(id=post.get_id())
                post.votes = submission.score
                comments = submission.comments
                post.num_comments = len(comments)
                
                comment_list = list()
                for comment in comments.list(): # NOTE: .list needed as otherwise only top level comments
                    comment_list.append(Comment(comment.id, comment.body, str(comment.score), comment.author.id, submission.id))
                
                # Push to kafka
                producer = KafkaProducer(bootstrap_servers='localhost:9092', key_serializer=lambda k: k.encode('utf-8'), value_serializer=lambda v: json.dumps(v).encode('utf-8'))
                for post in self._subreddit.get_posts():
                    for comment in comment_list:
                        producer.send('threads',key=str(post.get_id()), value=comment.to_dict())

                time.sleep(5)

    def stop(self):
        self._dead = True