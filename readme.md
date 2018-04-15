# Map Matching
[![](https://jitpack.io/v/GabrielCzar/mapmatching.svg)](https://jitpack.io/#GabrielCzar/mapmatching)

#### ADICIONANDO A DEPENDÊNCIA:  
Para disponibilização da biblioteca será utilizada a plataforma Jitpack, que compartilha a versão mais atualizada do repositório.
	
- Add the repository in ```pom.xml```:
	
```xml
<repositories>
  <repository>
  <id>jitpack.io</id>
  <url>https://jitpack.io</url>
  </repository>
</repositories>
```

- Add the dependency in ```pom.xml```:

```xml
<dependency>
    <groupId>com.github.GabrielCzar</groupId>
    <artifactId>MapMatching</artifactId>
    <version>1.2.0</version>
</dependency>
```
<details>
 <summary><h2>
  Using Docker Container Postgres
 </summary></h2>
 
- Criar local para armazenar dados

```docker volume create pg_data```

- Criar instancia do postgis
```shell
docker run --name=trajectory-data-postgis -d -e POSTGRES_USER=postgres -e POSTGRES_PASS=postgres -e POSTGRES_DBNAME=trajectory-data -e ALLOW_IP_RANGE=0.0.0.0/0 -p 5432:5432 -v pg_data:/var/lib/postgresql --restart=always kartoza/postgis:9.6-2.4
```
</details>

<details>
 <summary><h2>
  Extra Queries
 </summary></h2>

- Add column for geometry
```sql
alter table taxi_data add column geom geometry;
```

- Create table with taxis inside the osm
```sql
create table taxi_data_osm (
 id serial primary key,  
 taxi_id integer, 
 datetime timestamp,
 longitude double precision,
 latitude double precision
);
```
- Add column for id in taxi_data
```sql
alter table taxi_data add column id serial primary key;
```

- Create geometrys for cada taxi position
```sql
update taxi_data
set geom = ST_SetSRID(ST_MakePoint(t.long, t.lat), 4326)
from (
       select id, longitude as long, 
         latitude as lat from taxi_data) as t
WHERE t.id = taxi_data.id;

```

- Search by date interval
```sql
SELECT * FROM taxi_data WHERE date_time::date >= date '2008-02-02' AND date_time::date < date '2008-02-03';
``` 
</details>
