import tornado.websocket
from raven.contrib.tornado import SentryMixin

from .models import rpc_pb2


class RpcHandler(SentryMixin, tornado.websocket.WebSocketHandler):
    def post(self):
        request = rpc_pb2.RpcRequest()
        request.FromString(self.request.body)
        self.write(type(request))