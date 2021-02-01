# subreddit-analyzer
A tool for analyzing subreddits, both in real-time (dashboard) and in batch (OLAP).

## Current progress
- a Python script to gather data from Reddit has been created (uses PRAW -- https://praw.readthedocs.io/en/latest/)
- the Speed Layer, which uses Spring+Elastic Search+Kibana, has been created
- the Batch Layer, which uses Java+MongoDB+(Tableau), has been created

## How to run this tool
Move to the main folder of the project, and then run:
```
docker-compose up -d
```
Allow for up to a minute or so for all the services to be ready. After that, some additional configuration is required:

## Speed Layer
- Go to http://localhost:5601 (kibana).
- First import data with correct datetime zone
    - Go to left option bar > Stack Management > Advanced Settings > Timezone for date formatting > Set to Defaut (browser) > Reload page and check on discover sidebar menu that data are available. 
    - Go to left option bar > Stack Management > Advanced Settings > Timezone for date formatting > Set to UTC.
- For import kibana objects:
    - Go to left option bar > Stack Management > Advanced Settings > Saved Objects > Import file
- For visualize imported kibana dashboard:
    - Go to left option bar > Dashboard > Select imported dashboard to start visualize real-time data.
    - If data doesn't appear check selected dates at the top rigth of Kibana dahboard
        - **Note**: It is possibile to incurr in date error before of after changed timezone, dependending of browser default settings. If an error occur in dashboard ("Impossible to convert Browset datetime zone" or stuff like that), or dates are shifted of 1h, go to advanded settings and change datetime zone setted before.   
    
## Batch Layer
- Download the MongoDB BI Connector at https://docs.mongodb.com/bi-connector/v2.0/installation/
- Run the following commands:
```
mongodrdl --host localhost -d reddit_data -o schema.drdl
mongosqld --schema schema.drdl --mongo-uri localhost
```
- Open Tableau and connect to localhost:3307, you can now use data from reddit_data to do some OLAP.
