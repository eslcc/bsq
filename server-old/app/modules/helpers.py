from .redis import redis_client
from .models import gamestate_pb2, question_pb2


def redis_state_to_message(self):
    state = redis_client.hgetall('state')
    message = gamestate_pb2.GameState()
    message.state = gamestate_pb2.GameState.State.Value(state['state'.encode('utf8')])
    message.currentQuestion.CopyFrom(
        question_pb2.Question.FromString(
            redis_client.hget('questions', state['currentQuestion'.encode('utf8')])
        )
    )
    if not isinstance(self.current_user, str):  # for BIGSCREEN and ADMIN, current_user is a str
        message.myCurrentQuestionAnswer = redis_client.hget('answers', self.current_user.id)
    return message
