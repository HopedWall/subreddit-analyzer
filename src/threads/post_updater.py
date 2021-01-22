from threading import Thread
from entities.comment import Comment
from entities.redditor import Redditor
import time
import json
from kafka import KafkaProducer

class PostUpdater(Thread):
    def __init__(self, reddit, subreddit):
        Thread.__init__(self)
        self._reddit = reddit
        self._subreddit = subreddit
        self._dead = False
      
    def run(self):
        while not self._dead:

            producer = KafkaProducer(bootstrap_servers='localhost:9092', key_serializer=lambda k: k.encode('utf-8'), value_serializer=lambda v: json.dumps(v).encode('utf-8'))

            # Get a copy of posts list 
            posts = self._subreddit.get_posts()
            
            # Update comments and push them to kafka if necessary
            for post in posts:
                
                submission_handle = self._reddit.submission(id=post.get_id())
                post.votes = submission_handle.score
                comments_handle = submission_handle.comments
                post.num_comments = len(comments_handle)
                
                comments_list = list()
                for comment in comments_handle.list(): # NOTE: .list needed as otherwise only top level comments
                    try:
                        comments_list.append(Comment(comment.id, comment.body, str(comment.score), comment.author.id, post.get_id()))
                    except:
                        print("[WARN] Couldn't get comment")

                # Push to kafka
                for comment in comments_list:

                    ### COMMENT ###
                    # Store comment locally
                    commentPresent = comment in post.get_comments()

                    if not commentPresent:
                        # Add to tracked comments
                        post.add_comment(comment)

                    # Will send the whole post if never tracked before
                    # otherwise it will only send an update
                    producer.send('threads', key=str(post.get_id()), value=comment.to_dict(commentPresent))

                    #######################################################################################

                    ### AUTHOR ###
                    # Store author locally
                    user = Redditor(
                       comment.author.id,
                       comment.author.name,
                       comment.score)

                    userPresent = user in self._subreddit.get_users()

                    if not userPresent:
                        # Add to tracked users
                        self._subreddit.add_user(user)

                    # Will send the whole user if never tracked before
                    # otherwise it will only send an update
                    producer.send('threads', key=str(user.get_id()), value=user.to_dict(userPresent))

                time.sleep(2)

            time.sleep(30)

    def stop(self):
        self._dead = True