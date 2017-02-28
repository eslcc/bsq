@echo off
protoc -I=proto/models --python_out=server/app/modules/models proto/models/*.proto
robocopy proto server\app\proto /E
powershell -File fix-imports.ps1
echo Done
