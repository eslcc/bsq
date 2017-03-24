from os import environ

try:
    SENTRY_DSN = environ['SENTRY_DSN']
except KeyError:
    SENTRY_DSN = False

try:
    DEV = True if environ['DEV'] == 1 else False
except KeyError:
    DEV = False
