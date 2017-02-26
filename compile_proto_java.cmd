@echo off
protoc -I=proto/models --java_out=app/BigScienceQuiz/app/src/main/java proto/models/*.proto
