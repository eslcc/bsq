import tornado.websocket
from raven.contrib.tornado import SentryMixin
import unicodedata
from .models import rpc_pb2
from .redis import redis_client


def normalize_caseless(text):
    return unicodedata.normalize("NFKD", text.casefold())


class RpcHandler(SentryMixin, tornado.websocket.WebSocketHandler):
    def on_message(self, message):
        request = rpc_pb2.RpcRequest().FromString(message)
        field = request.WhichOneof('request')

        if field == 'autocompleteMemberNameRequest':
            filter_str = normalize_caseless(request.autocompleteMemberNameRequest.partialName)
            participants = redis_client.lrange('participant_names', 0, -1)
            matching = [x for x in participants if filter_str in normalize_caseless(x.decode('utf-8'))]

            response = rpc_pb2.RpcResponse()
            response.autocompleteMemberNameResponse.names.extend(matching)
            self.write_message(response.SerializeToString(), True)
        else:
            self.write_message('wat')
