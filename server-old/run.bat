@echo off
docker-compose up -d --build
docker-compose scale app=3
timeout 5
docker-compose restart lb
