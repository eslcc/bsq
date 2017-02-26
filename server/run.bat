@echo off
docker-compose up -d --build
docker-compose scale app=3 app_blue=3
docker-compose restart lb
