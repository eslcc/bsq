@echo off
docker-compose -f test-twoinstances.yaml up -d --build
docker-compose -f test-twoinstances.yaml scale app_junior=3 app_senior=3
docker-compose -f test-twoinstances.yaml restart lb_junior lb_senior
