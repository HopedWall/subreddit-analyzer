from threading import Thread
from comment import Comment
from redditor import Redditor
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
                
                comment_list = list()
                for comment in comments_handle.list(): # NOTE: .list needed as otherwise only top level comments
                    try:
                        comment_list.append(Comment(comment.id, comment.body, str(comment.score), comment.author.id, post.get_id()))
                    except:
                        print("[WARN] Couldn't get comment")

                # Push to kafka
                for comment in comment_list:

                    ### COMMENT ###
                    if comment not in post.get_comments():
                        post.add_comment(comment)
                        print(comment.to_dict())
                        producer.send('threads',key=str(post.get_id()), value=comment.to_dict())

                        ### AUTHOR ###
                        # This must be here since the user must be updated (locally) only if
                        # the comment was not already inserted

                        # Store author locally
                        user = Redditor(
                            comment.author.id,
                            comment.author.name,
                            comment.score)

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
                        print("Comment already being tracked")

                time.sleep(2)

            time.sleep(30)

    def stop(self):
        self._dead = True