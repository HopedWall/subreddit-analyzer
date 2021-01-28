# subreddit-analyzer
A tool for analyzing subreddits, both in real-time (dashboard) and in batch (sentiment analysis)

## Current progress
- a Python script to gather data from Reddit has been created (uses PRAW -- https://praw.readthedocs.io/en/latest/)

Requires Apache Kafka and Zookeper, this Docker image (https://hub.docker.com/r/johnnypark/kafka-zookeeper/) can be used to get Kafka up and running quickly.

## SpeedLayer
- Consumer for kafka topics and kibana dashboard based on elasticsearch.
- First: docker-compose up
- Go to http://localhost:5601 (kibana).
- First import data with correct datetime zone
    - Go to left option bar > Stack Management > Advanced Settings > Timezone for date formatting > Set to Defaut (browser) > Reload page and check on discover sidebar menu that data are available. 
    - Go to left option bar > Stack Management > Advanced Settings > Timezone for date formatting > Set to UTC.
- For import kibana objects:
    - Go to left option bar > Stack Management > Advanced Settings > Saved Objects > Import file