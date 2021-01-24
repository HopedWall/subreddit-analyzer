class Comment:
    def __init__(self, id, text, upvotes, author_id, author_name, thread_id):
        self._id = id
        self._text = text
        self._upvotes = upvotes
        self._author_id = author_id
        self._author_name = author_name
        self._thread_id = thread_id
    
    def update_votes(self, upvotes):
        self._upvotes = upvotes

    def add_upvotes(self, upvotes):
        self._upvotes = str(int(upvotes) + int(self._upvotes))
        
    def get_id(self):
        return self._id

    def get_text(self):
    	return self._text

    def get_upvotes(self):
        return self._upvotes

    def get_author_id(self):
    	return self._author_id

    def get_author_name(self):
        return self._author_name

    def get_thread_id(self):
        return self._thread_id

    # Override default equal implementation
    # useful to check if comment already downloaded
    def __eq__(self, other):
        if isinstance(other, Comment):
            return self._id == other._id
        return False

    def to_dict(self, alreadyPresent=False):
        _dict = {}
        if not alreadyPresent:  # create a dict that represents a newly created comment
            _dict['type'] = 'comment-create'
            for k, v in self.__dict__.items():
                if v is not None:
                    _dict[k] = v
        else:   # create a dict that represents un update to an existing comment
            _dict['type'] = 'comment-update'
            _dict['id'] = self._id
            _dict['upvotes'] = self._upvotes
        return _dict

    def to_dict_new(self):
        return self.to_dict(alreadyPresent=False)

    def to_dict_update(self):
        return self.to_dict(alreadyPresent=True)

