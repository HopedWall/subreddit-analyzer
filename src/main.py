import praw
from subreddit import Subreddit
from user_updater import UserUpdater
from post_downloader import PostDownloader
from post_updater import PostUpdater

if __name__ == '__main__':
    print("Start Python Analyzer")
    
    # Uses parameters in praw.ini
    reddit = praw.Reddit()
    
    # Set the subreddit to be used
    subreddit = Subreddit("destinythegame", 0, 0)

    # Download the 10 hottest post on the chosen subreddit
    post_downloader = PostDownloader(reddit, subreddit)
    post_downloader.start()
    
    # Update the comments of the posts currently being tracked
    post_updater = PostUpdater(reddit, subreddit)
    post_updater.start()
    
    # Download stats on the subreddit's redditors
    user_updater = UserUpdater(reddit, subreddit)
    user_updater.start()