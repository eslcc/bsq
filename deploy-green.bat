@echo off
color a
echo Deploying green env...
docker-compose up --no-recreate --build -d app
docker-compose scale app=3
timeout 5
docker-compose exec lb sed -i "s/bsqserver_app_blue/bsqserver_app/g" /etc/nginx/conf.d/default.conf
docker-compose kill -s HUP lb 
docker-compose -f docker-compose-blue.yaml stop app_blue
echo Deployment complete
