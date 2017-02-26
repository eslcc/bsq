import tornado.ioloop
import tornado.web
import tornado.websocket
import socket
from os import path
from raven.contrib.tornado import AsyncSentryClient, SentryMixin

from modules import EventHandler, RpcHandler, redis_client, env


if env.SENTRY_DSN:
    class BaseHandler(SentryMixin, tornado.web.RequestHandler):
        def write_error(self, status_code, **kwargs):
            if env.DEV:
                self.write(f'Error status: {status_code}<br>')
                self.write(kwargs['exc_info'])
            else:
                super().write_error(status_code, **kwargs)
else:
    class BaseHandler(tornado.web.RequestHandler):
        def write_error(self, status_code, **kwargs):
            if env.DEV:
                self.write(f'Error status: {status_code}<br>')
                self.write(kwargs['exc_info'])
            else:
                super().write_error(status_code, **kwargs)


class MainHandler(BaseHandler):
    def get(self):
        self.write("Hello world! I am server {}".format(socket.gethostname()))


class TestHandler(BaseHandler):
    def get(self):
        template_path = path.join(path.dirname(__file__), 'templates', 'test.html')
        self.render(template_path, messages=redis_client.lrange('messages', 1, -1), server=socket.gethostname())


class FuckupHandler(BaseHandler):
    def get(self):
        self.write("1 divided by 0 is {}".format(1/0))


class SocketHandler(EventHandler, RpcHandler):
    def __init__(self, application, request, **kwargs):
        super().__init__(application, request, **kwargs)

    def check_origin(self, origin):
        return True


def make_app():
    app = tornado.web.Application([
        (r"/", MainHandler),
        (r"/test", TestHandler),
        (r"/socket", SocketHandler),
        (r"/fuckup", FuckupHandler),
        (r"/proto/(.*)", tornado.web.StaticFileHandler, {'path': path.join(path.dirname(__file__), 'proto')})
    ])
    if env.SENTRY_DSN:
        app.sentry_client = AsyncSentryClient(
            env.SENTRY_DSN
        )
    return app


if __name__ == '__main__':
    app = make_app()
    port = 5000
    app.listen(port)
    print(f"App listening at port {port}")
    tornado.ioloop.IOLoop.current().start()
