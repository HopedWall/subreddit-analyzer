from threading import Thread
from entities.comment import Comment
from entities.redditor import Redditor
import time
import json
from kafka import KafkaProducer
import os

import config

class PostUpdater(Thread):
    def __init__(self, reddit, subreddit):
        Thread.__init__(self)
        self._reddit = reddit
        self._subreddit = subreddit
        self._dead = False
        self.url = os.getenv('KAFKA_CONTAINER', "localhost")
      
    def run(self):

        while not self._dead:

            print("URL:"+self.url+':9092')

            '''
            try:
                producer = KafkaProducer(bootstrap_servers=self.url+':9092', key_serializer=lambda k: k.encode('utf-8'), value_serializer=lambda v: json.dumps(v).encode('utf-8'))
            except:
                self._dead = True
                continue
            '''

            producer = KafkaProducer(bootstrap_servers=self.url+':9092', key_serializer=lambda k: k.encode('utf-8'), value_serializer=lambda v: json.dumps(v).encode('utf-8'))

            # Get a copy of posts list 
            posts = self._subreddit.get_posts()
            
            # Update comments and push them to kafka if necessary
            for post in posts:
                
                submission_handle = self._reddit.submission(id=post.get_id())
                post.votes = submission_handle.score
                comments_handle = submission_handle.comments
                post.num_comments = len(comments_handle)
                
                comments_list = list()
                #for comment in comments_handle.list(): # NOTE: .list needed as otherwise only top level comments
                for comment in comments_handle:
                    try:
                        comments_list.append(Comment(comment.id, comment.body, str(comment.score), comment.author.id, comment.author.name, post.get_id()))
                    except Exception as e:
                        print("[WARN] Couldn't get comment")
                        print("[WARN]", e)

                # Push to kafka
                for comment in comments_list:

                    ### COMMENT ###
                    # Store comment locally
                    commentPresent = comment in post.get_comments()

                    if not commentPresent:
                        # Add to tracked comments
                        post.append_comment(comment)
                        # Send new comment to kafka
                        print(comment.to_dict(commentPresent))
                        producer.send('threads', key=str(post.get_id()), value=comment.to_dict_new())

                        print("comments before:",config.curr_comments)
                        config.curr_comments += 1
                        print("comments after:",config.curr_comments)
                        config.created_comments += 1
                    else:
                        # Get last post update
                        lastCommentUpdate = post.get_comment(comment)
                        # Only push to kafka if upvotes have changed
                        commentNotUpdated = comment.get_upvotes() != lastCommentUpdate.get_upvotes()
                        
                        if commentNotUpdated: 
                            # Update local post
                            post.update_comment(lastCommentUpdate, comment)
                            # Send updated post to kafka
                            print(comment.to_dict(commentPresent))
                            producer.send('threads', key=str(post.get_id()), value=comment.to_dict_update())

                            config.updated_comments += 1

                    #######################################################################################

                    ### AUTHOR ###
                    # Store author locally
                    user = Redditor(
                       comment.get_author_id(),
                       comment.get_author_name(),
                       comment.get_upvotes())

                    userPresent = user in self._subreddit.get_users()

                    if not userPresent:
                        # Add to tracked users
                        self._subreddit.add_user(user)
                        # Send new user to kafka
                        print(user.to_dict(userPresent))
                        producer.send('users', key=str(user.get_id()), value=user.to_dict_new())

                        config.curr_users += 1
                        config.created_users += 1
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

                            config.updated_users += 1

                #time.sleep(2)

            time.sleep(30)

    def stop(self):
        self._dead = True