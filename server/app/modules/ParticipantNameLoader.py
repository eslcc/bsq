import os
from .redis import redis_client


def load_names():
    print('Starting participant name load:')
    redis_client.delete('participant_names')
    with open(os.path.join(os.path.dirname(__file__), os.pardir, 'participant_names.txt'), 'r') as f:
        content = f.readlines()
        content = [x.strip() for x in content]
        pipe = redis_client.pipeline()
        for name in content:
            pipe.rpush('participant_names', name)
        pipe.execute()
        print('Done.')
