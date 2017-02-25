import tornado.ioloop
import tornado.web
import tornado.gen
import tornado.websocket
from tornado.template import Template
import tornadoredis
import tornadoredis.pubsub
import socket
import redis
from os import path
from raven.contrib.tornado import AsyncSentryClient, SentryMixin

redis_client = redis.StrictRedis(host='redis',port=6379,db=0)


class MainHandler(SentryMixin, tornado.web.RequestHandler):
    def get(self):
        self.write("Hello world! I am server {}".format(socket.gethostname()))


class TestHandler(SentryMixin, tornado.web.RequestHandler):
    def get(self):
        f = open(path.join(path.dirname(__file__), 'templates', 'test.html'), 'r')
        t = Template(f.read())
        self.write(t.generate(server=socket.gethostname(), messages=redis_client.lrange('messages', 0, -1)))
        f.close()


class FuckupHandler(SentryMixin, tornado.web.RequestHandler):
    def get(self):
        self.write("1 divided by 0 is {}".format(1/0))


class SocketHandler(SentryMixin, tornado.websocket.WebSocketHandler):
    def __init__(self, application, request, **kwargs):
        super().__init__(application, request, **kwargs)
        self.client = tornadoredis.Client(host='redis')

    def check_origin(self, origin):
        return True

    @tornado.gen.engine
    def open(self):
        self.client.connect()
        yield tornado.gen.Task(self.client.subscribe, 'messages')
        self.client.listen(self.on_redis)

    def on_redis(self, msg):
        if msg.kind == 'message':
            self.write_message(str(msg.body))
        if msg.kind == 'disconnect':
            # Do not try to reconnect, just send a message back
            # to the client and close the client connection
            self.write_message('The connection terminated '
                               'due to a Redis server error.')
            self.close()

    def on_message(self, msg):
        redis_client.rpush('messages', msg)
        redis_client.publish('messages', msg)

    def on_close(self):
        if self.client.subscribed:
            self.client.unsubscribe('messages')
            self.client.disconnect()


def make_app():
    app =  tornado.web.Application([
        (r"/", MainHandler),
        (r"/test", TestHandler),
        (r"/socket", SocketHandler),
        (r"/fuckup", FuckupHandler)
    ])
    app.sentry_client = AsyncSentryClient(
        'http://8bc6211b74be4a689d9dfb4864285a72:be9c429a23764fd19d6ec0873ec9811c@sentry:9000/2'
    )
    return app


if __name__ == '__main__':
    app = make_app()
    app.listen(5000)
    tornado.ioloop.IOLoop.current().start()
