class Subreddit:
    def __init__(self, name, visitors, active_users):
        self._name = name
        self._visitors = visitors
        self._act_users = active_users
        self._posts = list()
    
    def update_visitors(self, visitors):
        self._visitors = visitors
    
    def update_active_users(self, users):
        self._act_users = users
        
    def get_name(self):
        return self._name

    def get_visitors(self):
        return self._visitors
    
    def get_active_users(self):
        return self._act_users
    
    def append_post(self, post):
        self._posts.append(post)

    def get_posts(self):
        return self._posts

    # Returns all the comments from all the threads in the subreddit
    def get_all_comments(self):
        comments = list()

        for post in self._posts:
            for comment in post.get_comment_list():
                comments.append(comment)

        return comments
