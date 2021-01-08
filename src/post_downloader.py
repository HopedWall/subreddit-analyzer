from threading import Thread
from post import Post
from comment import Comment
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
        sub = self._reddit.subreddit(self._subreddit.get_name())    
        
        # Skyp existing: for submission in sub.stream.submissions(skip_existing=True):
        #for submission in sub.stream.submissions(skip_existing=True):
        # WARN: skip_existing causes troubles!

        # TODO: scarica solo i 10 post più hot
        # TODO2: tieni traccia dei post già scaricati per non pusharli più volte
        
        for submission in sub.stream.submissions(skip_existing=True):
            # Sorted for confidence
            comments = submission.comments

            comment_list = list()
            for comment in comments.list(): # NOTE: .list needed as otherwise only top level comments
                try:
                    comment_list.append(Comment(comment.id, comment.body, str(comment.score), comment.author.id, submission.id))
                except:
                    print("[WARN] Couldn't get comment")
                #print(type(comment.id), type(comment.body), type(comment.score), type(comment.author.id), type(submission.id))

            post = Post(
                submission.id,
                submission.url, 
                submission.author.name, # name
                submission.link_flair_text, 
                str(submission.score), 
                submission.title)

            #print(type(post.get_id()), type(post.get_url()), type(post.get_author()), type(post.get_flair()), type(post.get_score()), type(post.get_title()))
            
            self._subreddit.append_post(post)
            
            # Push to kafka
            producer = KafkaProducer(bootstrap_servers='localhost:9092', key_serializer=lambda k: k.encode('utf-8'), value_serializer=lambda v: json.dumps(v).encode('utf-8'))
            for post in self._subreddit.get_posts():
                producer.send('threads',key=str(post.get_id()),value=post.to_dict())
                for comment in comment_list:
                    producer.send('threads',key=str(post.get_id()), value=comment.to_dict())
            
            time.sleep(2) # 2 seconds of sleep

    def stop(self):
        self._dead = True