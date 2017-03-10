import tornado.ioloop
import tornado.options
import tornado.web
import tornado.websocket
import socket
from os import path
import logging
from raven.contrib.tornado import AsyncSentryClient, SentryMixin

from modules import EventHandler, RpcHandler, redis_client, env, question_loader, state_initialize

STARTUP_CALLBACKS = [
    question_loader,
    state_initialize
]

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


class NocacheStaticFileHandler(tornado.web.StaticFileHandler):
    def set_extra_headers(self, path):
        # Disable cache
        self.set_header('Cache-Control', 'no-store, no-cache, must-revalidate, max-age=0')

class MainHandler(BaseHandler):
    def get(self):
        self.write("Hello world! I am server {}".format(socket.gethostname()))


class TestHandler(BaseHandler):
    def get(self):
        template_path = path.join(path.dirname(__file__), 'templates', 'test.html')
        self.render(template_path, server=socket.gethostname())


class FuckupHandler(BaseHandler):
    def get(self):
        self.write("1 divided by 0 is {}".format(1/0))


class AdminHandler(BaseHandler):
    def get(self):
        template_path = path.join(path.dirname(__file__), 'templates', 'admin.html')
        self.render(template_path)

class SocketHandler(EventHandler, RpcHandler):
    def __init__(self, application, request, **kwargs):
        super().__init__(application, request, **kwargs)

    def check_origin(self, origin):
        return True


def make_app():
    tornado.options.parse_config_file(path.join(path.dirname(__file__), 'server.conf'))
    tornado.options.parse_command_line()
    app = tornado.web.Application([
        (r"/", MainHandler),
        (r"/test", TestHandler),
        (r"/socket", SocketHandler),
        (r"/fuckup", FuckupHandler),
        (r"/proto/(.*)", NocacheStaticFileHandler, {'path': path.join(path.dirname(__file__), 'proto')}),
        (r"/js/(.*)", NocacheStaticFileHandler, {'path': path.join(path.dirname(__file__), 'js')}),
        (r"/admin", AdminHandler)
    ])
    if env.SENTRY_DSN:
        app.sentry_client = AsyncSentryClient(
            env.SENTRY_DSN
        )
    return app


if __name__ == '__main__':
    app_log = logging.getLogger("tornado.application")
    app = make_app()
    port = 5000

    for callback in STARTUP_CALLBACKS:
        callback()

    app.listen(port)
    app_log.info(f"App listening at port {port}")
    tornado.ioloop.IOLoop.current().start()
