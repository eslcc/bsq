from .redis import redis_client

def initialize():
    if not redis_client.exists('state'):
        redis_client.hset('state', 'state', 'NOTREADY')
        redis_client.hset('state', 'currentQuestion', None)
