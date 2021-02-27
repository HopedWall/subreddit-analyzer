import praw
from entities.subreddit import Subreddit
from threads.subreddit_updater import SubredditUpdater
from threads.post_downloader import PostDownloader
from threads.post_updater import PostUpdater
import os
import time
from datetime import datetime

import config

if __name__ == '__main__':
    print("Start Python Analyzer")
    
    # Uses parameters in praw.ini
    reddit = praw.Reddit()
    
    # Set the subreddit to be used
    subreddit = Subreddit("wallstreetbets", 0, 0)

    # Download the 10 hottest post on the chosen subreddit
    post_downloader = PostDownloader(reddit, subreddit)
    post_downloader.start()
    
    # Update the comments of the posts currently being tracked
    post_updater = PostUpdater(reddit, subreddit)
    post_updater.start()
    
    # Download stats on the subreddit's redditors
    subreddit_updater = SubredditUpdater(reddit, subreddit)
    subreddit_updater.start()

    while(True):
        current_time = datetime.now().strftime("%d/%m/%Y-%H:%M:%S")
        with open("stats.txt", 'a+') as f:
            f.write("{},{},{},{},{},{},{},{},{},{}\n".format(current_time, 
                                                        config.curr_threads, config.created_threads, config.updated_threads, 
                                                        config.curr_comments, config.created_comments, config.updated_comments,
                                                        config.curr_users, config.created_users, config.updated_users))
        time.sleep(60)