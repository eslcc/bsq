docker-compose up -d --build
docker-compose scale app=3
docker-compose restart lb
