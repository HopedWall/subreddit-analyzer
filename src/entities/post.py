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

    def add_comment(self, comment):
        self._comments.append(comment)

    # Override default equal implementation
    # useful to check if post already downloaded
    def __eq__(self, other):
        if isinstance(other, Post):
            return self._id == other._id and self._upvotes == other._upvotes
        return False

    def to_dict(self):
        _dict = {}
        _dict['type'] = 'post'
        for k, v in self.__dict__.items():
            # Do not send the comments, will be retrieved via kafka
            if v is not None and k != '_comments':
                _dict[k] = v
        return _dict

