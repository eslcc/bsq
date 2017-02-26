import tornado.websocket
from raven.contrib.tornado import SentryMixin

from .models import rpc_pb2


class RpcHandler(SentryMixin, tornado.websocket.WebSocketHandler):
    def on_message(self, message):
        request = rpc_pb2.RpcRequest().FromString(message)
        self.write_message(f"Bytes {message} request {request} field {request.WhichOneof('request')}", False)
