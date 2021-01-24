class Redditor:
    def __init__(self, id, username, upvotes):
        self._id = id
        self._username = username
        self._upvotes = upvotes

    def add_upvotes(self, upvotes):
        self._upvotes = str(int(upvotes) + int(self._upvotes))

    def get_upvotes(self):
        return self._upvotes

    def get_id(self):
        return self._id

    # Override default equal implementation
    # useful to check if user already inserted
    def __eq__(self, other):
        if isinstance(other, Redditor):
            return self._id == other._id
        return False

    def to_dict(self, alreadyPresent=False):
        _dict = {}
        if not alreadyPresent:  # create a dict that represents a newly created redditor
            _dict['type'] = 'user-create'
            for k, v in self.__dict__.items():
                if v is not None:
                    _dict[k] = v
        else:   # create a dict that represents un update to an existing redditor
            _dict['type'] = 'user-update'
            _dict['id'] = self._id
            _dict['upvotes'] = self._upvotes
        return _dict

    def to_dict_new(self):
        self.to_dict(alreadyPresent=False)

    def to_dict_update(self):
        self.to_dict(alreadyPresent=True)
