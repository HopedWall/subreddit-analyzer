class Post:
    def __init__(self, id, url, author, flairs, upvotes, title):
        self._id = id
        self._url = url
        self._author = author
        self._flairs = flairs
        self._upvotes = upvotes
        self._title = title

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

    def get_score(self):
        return self._upvotes

    def to_dict(self):
        _dict = {}
        _dict['type'] = 'post'
        for k, v in self.__dict__.items():
            if v is not None:
                _dict[k] = v
        return _dict

