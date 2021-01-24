# NOTE: Only used for local storage!
class Subreddit:
    def __init__(self, name, visitors, active_users):
        self._name = name
        self._visitors = visitors
        self._act_users = active_users
        self._posts = list()
        self._users = list()
    
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

    def get_users(self):
        return self._users

    def add_user(self, user):
        self._users.append(user)

    
    def get_post(self, post):
        if post in self._posts:
            pos = self._posts.index(post)
            return self._posts[pos]
        else:
            return None

    def delete_post(self, post):
        self._posts.remove(post)

    def update_post(self, oldPost, newPost):
        # Removes the OLD version of the post
        self.delete_post(oldPost)
        # Gets old post comments
        oldComments = oldPost.get_comments()
        # Sets the comments on the new post
        newPost.set_comments(oldComments)
        # Adds the NEW version of the post
        self.append_post(newPost)

    def append_post(self, post):
        self._posts.append(post)

    def get_user(self, user):
        if user in self._users:
            pos = self._users.index(user)
            return self._users[pos]
        else:
            return None

    def delete_user(self, user):
        self._users.remove(user)

    def update_user(self, oldUser, newUser):
        # Removes the OLD version of the post
        self.delete_user(oldUser)
        # Gets the old user upvotes
        oldUpvotes = oldUser.get_upvotes()
        # Update upvotes count for new user
        # TODO: check this carefully!!!!
        newUser.add_upvotes(oldUpvotes)
        # Adds the NEW version of the post
        self.append_user(newUser)

    def append_user(self, user):
        self._users.append(user)

    # Returns all the comments from all the threads in the subreddit
    def get_all_comments(self):
        comments = list()

        for post in self._posts:
            for comment in post.get_comment_list():
                comments.append(comment)

        return comments
