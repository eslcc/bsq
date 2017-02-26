@echo off
docker-compose -f docker-compose-dev.yaml up -d --build
docker-compose -f docker-compose-dev.yaml scale app=3
docker-compose -f docker-compose-dev.yaml restart lb
