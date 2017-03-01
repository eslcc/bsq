from ..redis import redis_client
from ..models import rpc_pb2, question_pb2
from .. import helpers


def get_questions(self, request):
    response = rpc_pb2.RpcResponse()
    if not self.current_user == 'ADMIN':
        response.unauthorisedRequestResponse.reason = 'Not admin'
    else:
        questions = redis_client.hgetall('questions')
        question_messages = [question_pb2.Question().FromString(x) for x in questions.values()]
        print(question_messages)
        response.adminGetQuestionsResponse.questions.extend(question_messages)
    return response


def set_active_question(self, request):
    id = request.adminSetActiveQuestionRequest.questionId
    redis_client.hset('state', 'currentQuestion', id)
    redis_client.publish('game_events', f'game_state_change{id}')
    response = rpc_pb2.RpcResponse()
    response.adminSetActiveQuestionResponse.newState.CopyFrom(helpers.redis_state_to_message(self))
    return response
