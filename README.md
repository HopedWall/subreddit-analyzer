# subreddit-analyzer
A tool for analyzing subreddits, both in real-time (dashboard) and in batch (sentiment analysis)

## Current progress
- a Python script to gather data from Reddit has been created (uses PRAW -- https://praw.readthedocs.io/en/latest/)

Requires Apache Kafka and Zookeper, this Docker image (https://hub.docker.com/r/johnnypark/kafka-zookeeper/) can be used to get Kafka up and running quickly.