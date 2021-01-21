class Redditor:
    def __init__(self, id, username, upvotes):
        self._id = id
        self._username = username
        self._total_upvotes = upvotes

    def add_upvotes(self, upvotes):
        self._total_upvotes += upvotes

    def get_upvotes(self):
        return self._total_upvotes

    def get_id(self):
        return self._id

    # Override default equal implementation
    # useful to check if user already inserted
    def __eq__(self, other):
        if isinstance(other, Redditor):
            return self._id == other._id and self._total_upvotes == other._total_upvotes
        return False

    def to_dict(self):
        _dict = {}
        _dict['type'] = 'user'
        for k, v in self.__dict__.items():
            if v is not None:
                _dict[k] = v
        return _dict