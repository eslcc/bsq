@echo off
protoc -I=proto --python_out=server/app/modules proto/models/*.proto
robocopy proto server\app\proto /E
echo Note: Remember to edit the generated source to change to Python 3 relative imports
echo (change `from models import ...` to `from . import ...`)
