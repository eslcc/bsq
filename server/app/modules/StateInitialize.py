from .redis import redis_client

def initialize():
    redis_client.hset('state', 'state', 'NOTREADY')
    redis_client.hset('state', 'currentQuestion', None)
