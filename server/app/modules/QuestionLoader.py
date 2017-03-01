import os
from .redis import redis_client
from .models.question_pb2 import Question
from google.protobuf import json_format


def load_questions():
    print('Starting question load...')
    redis_client.delete('questions')
    with open(os.path.join(os.path.dirname(__file__), os.pardir, 'questions.json'), 'r') as f:
        content = f.read()
        content = content.split(';;;;')
        items = {}
        for json in content:
            q = Question()
            json_format.Parse(json, q)
            items[q.id] = q.SerializeToString()
        redis_client.hmset('questions', items)
