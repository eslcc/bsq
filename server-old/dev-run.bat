@echo off
docker-compose -f docker-compose-dev.yaml up -d --build
docker-compose -f docker-compose-dev.yaml scale app=3
timeout 5
docker-compose -f docker-compose-dev.yaml restart lb
