@echo off
color a
echo Deploying green env...
docker-compose up --no-recreate --build -d app
docker-compose scale app=3
timeout 5
docker-compose exec lb sed -i "s/server_app_blue/server_app/g" /etc/nginx/conf.d/default.conf
docker-compose kill -s HUP lb 
docker-compose -f blue.yaml stop app_blue
echo Deployment complete
