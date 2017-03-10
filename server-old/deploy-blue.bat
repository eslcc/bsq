@echo off
color b
echo Deploying blue env...
docker-compose -f blue.yaml up --build --no-recreate -d app_blue
docker-compose -f blue.yaml scale app_blue=3
timeout 5
docker-compose exec lb sed -i "s/server_app/server_app_blue/g" /etc/nginx/conf.d/default.conf
docker-compose kill -s HUP lb
docker-compose stop app
echo Deployment complete
