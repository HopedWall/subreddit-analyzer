class Post:
    def __init__(self, id, url, author, flairs, upvotes, title, text):
        self._id = id
        self._url = url
        self._author = author
        self._flairs = flairs
        self._upvotes = upvotes
        self._title = title
        self._text = text
        self._comments = [] 
        # NOTE: posts start off with 0 comments in post_downloader, 
        # they get added by post_updater

    def get_id(self):
        return self._id

    def get_url(self):
        return self._url
    
    def update_votes(self, votes):
        self._votes = votes

    def get_title(self):
        return self._title

    def get_author(self):
        return self._author

    def get_upvotes(self):
        return self._upvotes

    def get_comments(self):
        return self._comments

    def append_comment(self, comment):
        self._comments.append(comment)

    def set_comments(self, otherComments):
        self._comments = otherComments

    def delete_comment(self, comment):
        self._comments.remove(comment)

    def get_comment(self, comment):
        if comment in self._comments:
            pos = self._comments.index(comment)
            return self._comments[pos]
        else:
            return None

    def update_comment(self, oldComment, newComment):
        # Removes the OLD version of the comment
        self.delete_comment(oldComment)
        # Gets the upvotes of the old comment
        oldUpvotes = oldComment.get_upvotes()
        # Updates the upvotes of the new comment
        newComment.add_upvotes(oldUpvotes)
        # Adds the NEW version of the comment
        self.append_comment(newComment)

    def get_text(self):
        return self._text

    # Override default equal implementation
    # useful to check if post already downloaded
    def __eq__(self, other):
        if isinstance(other, Post):
            return self._id == other._id
        return False

    def to_dict(self, alreadyPresent=False):
        _dict = {}
        if not alreadyPresent:  # create a dict that represents a newly created post
            _dict['type'] = 'post-create'
            for k, v in self.__dict__.items():
                # Do not send the comments, will be retrieved via kafka
                if v is not None and k != '_comments':
                    _dict[k] = v
        else:   # create a dict that represents un update to an existing post
            _dict['type'] = 'post-update'
            _dict['id'] = self._id
            _dict['upvotes'] = self._upvotes
        return _dict

    def to_dict_new(self):
        return self.to_dict(alreadyPresent=False)

    def to_dict_update(self):
        return self.to_dict(alreadyPresent=True)


