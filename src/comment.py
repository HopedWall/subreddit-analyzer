class Comment:
    def __init__(self, id, text, votes, author, thread_id):
        self._id = id
        self._text = text
        self._votes = votes
        self._author = author
        self._thread_id = thread_id
    
    def update_votes(self, votes):
        self._votes = votes
        
    def get_id(self):
        return self._id

    def get_text(self):
    	return self._text

    def get_author(self):
    	return self._author

    def get_thread_id(self):
        return self._thread_id

    def to_dict(self):
        _dict = {}
        _dict['type'] = 'comment'
        for k, v in self.__dict__.items():
            if v is not None:
                _dict[k] = v
        return _dict