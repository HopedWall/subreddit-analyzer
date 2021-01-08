import praw

reddit = praw.Reddit()

# Check if connection is working
#print(reddit.read_only)  # Output: True

# Get 10 newest (.new) threads in /r/destinythegame
# we could also use .hot, .top, etc.
for submission in reddit.subreddit("destinythegame").new(limit=10):
	print('FLAIR: '+submission.link_flair_text)
	print('AUTHOR: '+submission.author.name)
	print('UPVOTES: '+str(submission.score)+' ')
	print('TITLE: '+submission.title)
	print('URL: ' + submission.url)

	# Get comments for given thread
	comments = submission.comments

	# Print forst comment sorted for default metric: confidence
	if comments:
		print('FIRST COMMENT IN THREAD: '+comments[0].body+' BY '+comments[0].author.name+'\n')
	else:
		print('NO COMMENT YET!'+'\n')
  
for submission in reddit.subreddit("destinythegame").new(limit=10):
	print('FLAIR: '+submission.link_flair_text)
	print('AUTHOR: '+submission.author.name)
	print('UPVOTES: '+str(submission.score)+' ')
	print('TITLE: '+submission.title)
	print('URL: ' + submission.url)

	# Get comments for given thread
	comments = submission.comments

	# Print forst comment sorted for default metric: confidence
	if comments:
		print('FIRST COMMENT IN THREAD: ' + comments[0].body+' BY '+comments[0].author.name+'\n')
		print(''+str(+comments[0].score))
	else:
		print('NO COMMENT YET!'+'\n')

