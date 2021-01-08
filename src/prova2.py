import praw
from time import sleep

reddit = praw.Reddit()

# Check if connection is working
#print(reddit.read_only)  # Output: True

# Get 10 newest (.new) threads in /r/destinythegame
# we could also use .hot, .top, etc.
# submissions = []
# for submission in reddit.subreddit("destinythegame").hot(limit=1):
# 	submissions.append(submission)
# 	print(vars(submission).keys())
# 	# print('FLAIR: '+submission.link_flair_text)
# 	# print('AUTHOR: '+submission.author.name)
# 	# print('UPVOTES: '+str(submission.score)+' ')
# 	# print('TITLE: '+submission.title)

# for i in range(100):
# 	submission = reddit.submission(id=submissions[0].id)
# 	print('Score by API '+str(submission.score))
# 	print('Stored score '+str(submissions[0].score))
# 	time.sleep(1)

# 	# Get comments for given thread
# 	comments = submission.comments

# 	if comments:
# 		print('FIRST COMMENT IN THREAD: '+comments[0].body+' BY '+comments[0].author.name+'\n')
# 	else:
# 		print('NO COMMENT YET!'+'\n')

#for comment in reddit.subreddit('destinythegame').stream.comments():
#    print(comment.body+'\n')

#for submission in reddit.subreddit('all').stream.submissions():
#    print(submission.title+' '+"BY "+str(submission.author)+'\n'+'\n')

# thread = reddit.live("ukaeu1ik4sw5")
# update = thread["7827987a-c998-11e4-a0b9-22000b6a88d2"]
# update.thread  # LiveThread(id="ukaeu1ik4sw5")
# update.id  # "7827987a-c998-11e4-a0b9-22000b6a88d2"
# update.author  # "umbrae"

#thread = reddit.live("ukaeu1ik4sw5")
#for submission in thread.discussions(limit=None):
#    print(submission.title)

dtg = reddit.subreddit("askreddit")
for i in range(10):
	# MUST refresh subreddit to get actual value, otherwise it's not updated
	dtg = reddit.subreddit("askreddit")

	print("Active accounts: {}".format(dtg.accounts_active))

	# Try once every 5 minutes
	sleep(5*60)



#import requests
#headers = {
#    "User-Agent": "don't rate limit me"
#}

# def get_active_users(subreddit):
#     url = "http://www.reddit.com/r/{}/about.json".format(subreddit)
#     resp = requests.get(url, headers=headers)
#     #if not resp.ok:
#         # handle request error, return -1?
#         #return -1
#     content = resp.json()
#     print(content.keys())
#     print(content["kind"])
#     print(content["data"].keys())


# get_active_users("destinythegame")



