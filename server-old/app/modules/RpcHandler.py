import tornado.websocket
from raven.contrib.tornado import SentryMixin
from .models import rpc_pb2

from . import rpc_handlers

HANDLERS = {
    'identifyUserRequest': rpc_handlers.identify_user,
    'autocompleteMemberNameRequest': rpc_handlers.autocomplete_member_name,
    'adminGetQuestionsRequest': rpc_handlers.get_questions,
    'adminSetActiveQuestionRequest': rpc_handlers.set_active_question,
    'adminResetStateRequest': rpc_handlers.reset_state,
    'getGameStateRequest': rpc_handlers.get_game_state
}


class RpcHandler(SentryMixin, tornado.websocket.WebSocketHandler):
    def on_message(self, message):
        request = rpc_pb2.RpcRequest().FromString(message)
        field = request.WhichOneof('request')
        if field in HANDLERS:
            response = HANDLERS[field](self, request)
        else:
            response = rpc_pb2.RpcResponse()
            response.unknownRequestResponse.request = f"request {field}"
        self.write_message(response.SerializeToString(), True)


    def select_subprotocol(self, subprotocols):
        if 'admin' in subprotocols:
            self.current_user = 'ADMIN'
            return 'admin'
        else:
            return None
