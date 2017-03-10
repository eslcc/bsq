import traceback
import tornado.websocket
import tornado.gen
import logging
import tornadoredis
from raven.contrib.tornado import SentryMixin

from .models import events_pb2
from . import helpers

app_log = logging.getLogger("tornado.application")


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
        app_log.critical('on_redis called')
        app_log.critical(f"message kind {message.kind}")
        try:
            to_write = None
            if message.kind == 'message':
                app_log.critical(f"c: {message.channel.decode('utf8')}, b: {message.body.decode('utf8')}")
                # if message.channel == 'game_events'.encode('utf8'):
                #     if message.body == 'game_state_change'.encode('utf8'):
                to_write = events_pb2.GameEvent()
                to_write.gameStateChangeResponse.newState = helpers.redis_state_to_message(self)
            elif message.kind == 'disconnect':
                to_write = events_pb2.GameEvent()
                to_write.errorEvent.description = 'Subscription disconnected from Redis'
            self.write_message(to_write.SerializeToString(), binary=True)
        except Exception as e:
            to_write = events_pb2.GameEvent()
            to_write.errorEvent.description = traceback.format_exc()
            self.write_message(to_write.SerializeToString(), binary=True)

    def on_close(self):
        if self.client.subscribed:
            self.client.unsubscribe('system_messages')
            self.client.unsubscribe('game_events')
            self.client.disconnect()
