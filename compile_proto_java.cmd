@echo off
protoc -I=proto/models --java_out=app/BigScienceQuiz/app/src/main/java proto/models/*.proto
protoc -I=proto/models --java_out=server/src/main/java proto/models/*.proto
