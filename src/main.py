import praw
from subreddit import Subreddit
from user_updater import UserUpdater
from post_downloader import PostDownloader
from post_updater import PostUpdater

if __name__ == '__main__':
    print("Start Python Analyzer")
    
    # Uses parameters in praw.ini
    reddit = praw.Reddit()
    
    subreddit = Subreddit("all", 0, 0)
    
    user_updater = UserUpdater(reddit, subreddit)
    user_updater.start()
    
    post_downloader = PostDownloader(reddit, subreddit)
    post_downloader.start()
    
    votes_updater = VotesUpdater(reddit, subreddit)
    votes_updater.start()