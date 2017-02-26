import tornado.websocket
import tornado.gen
import tornadoredis
from raven.contrib.tornado import SentryMixin

from .models import events_pb2


class EventHandler(SentryMixin, tornado.websocket.WebSocketHandler):

    def __init__(self, application, request, **kwargs):
        super().__init__(application, request, **kwargs)
        self.listen()

    @tornado.gen.engine
    def listen(self):
        # noinspection PyAttributeOutsideInit
        self.client = tornadoredis.Client(host='redis')
        self.client.connect()
        yield tornado.gen.Task(self.client.subscribe, 'system_messages')
        yield tornado.gen.Task(self.client.subscribe, 'game_events')
        self.client.listen(self.on_redis)

    def on_redis(self, message):
        to_write = None
        if message.kind == 'message':
            if message.channel == 'game_events':
                pass
        elif message.kind == 'disconnect':
            to_write = events_pb2.ErrorEvent()
            to_write.description = 'Subscription disconnected from Redis'
        self.write_message(to_write.SerializeToString(), binary=True)

    def on_close(self):
        if self.client.subscribed:
            self.client.unsubscribe('system_messages')
            self.client.unsubscribe('game_events')
            self.client.disconnect()
