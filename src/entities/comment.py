class Comment:
    def __init__(self, id, text, upvotes, author, thread_id):
        self._id = id
        self._text = text
        self._upvotes = upvotes
        self._author = author
        self._thread_id = thread_id
    
    def update_votes(self, upvotes):
        self._upvotes = upvotes
        
    def get_id(self):
        return self._id

    def get_text(self):
    	return self._text

    def get_upvotes(self):
        return self._upvotes

    def get_author(self):
    	return self._author

    def get_thread_id(self):
        return self._thread_id

    # Override default equal implementation
    # useful to check if comment already downloaded
    def __eq__(self, other):
        if isinstance(other, Comment):
            return self._id == other._id and self._upvotes == other._upvotes
        return False

    def to_dict(self):
        _dict = {}
        _dict['type'] = 'comment'
        for k, v in self.__dict__.items():
            if v is not None:
                _dict[k] = v
        return _dict