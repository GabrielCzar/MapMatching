# using docker postgres for data

### Criar local para armazenar dados
docker volume create pg_data

### Criar instancia do postgis
docker run --name=trajectory-data-postgis -d -e POSTGRES_USER=postgres -e POSTGRES_PASS=postgres -e POSTGRES_DBNAME=trajectory-data -e ALLOW_IP_RANGE=0.0.0.0/0 -p 5432:5432 -v pg_data:/var/lib/postgresql --restart=always kartoza/postgis:9.6-2.4
