# Map Matching
[![](https://jitpack.io/v/GabrielCzar/MapMatching.svg)](https://jitpack.io/#GabrielCzar/MapMatching)

#### ADICIONANDO A DEPENDÊNCIA:  
To make a library available, a Jitpack platform will be used, which shares a more original version of the repository.
	
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
    <groupId>com.github.gabrielczar</groupId>
    <artifactId>MapMatching</artifactId>
    <version>1.3.0</version>
</dependency>
```
<details>
 <summary><h2>
  Using Docker Container Postgres
 </summary></h2>
 
- Create local to save postgres data

```docker volume create pg_data```

- Create instance posgres with postgis extension
```shell
docker run --name=trajectory-data-postgis -d -e POSTGRES_USER=postgres -e POSTGRES_PASS=postgres -e POSTGRES_DBNAME=trajectory-data -e ALLOW_IP_RANGE=0.0.0.0/0 -p 5432:5432 -v pg_data:/var/lib/postgresql --restart=always kartoza/postgis:9.6-2.4
```
</details>

<details>
 <summary><h2>
  Extra Queries
 </summary></h2>

#### Create table 

- Create table to save output
```sql
CREATE TABLE matched_points (
	id serial primary key,
	natural_id integer, 
 	datetime timestamp,
 	longitude double precision,
 	latitude double precision,
	edge_id bigint,
	offset double precision,
	geometry point
);
```

- Create table with content
```sql
CREATE TABLE dataset (
 id serial primary key,  
 natural_id integer, 
 datetime timestamp,
 longitude double precision,
 latitude double precision
);
```

#### Aditional columns

- Add column for geometry
```sql
ALTER TABLE dataset ADD COLUMN geom GEOMETRY;
```

- Add aditional column for serial id in dataset
```sql
ALTER TABLE dataset ADD COLUMN ID SERIAL PRIMARY KEY;
```

#### Aditional update values 

- Create geometrys for each dataset location
```sql
update dataset
set geom = ST_SetSRID(ST_MakePoint(t.long, t.lat), 4326)
from (
       select id, longitude as long, 
         latitude as lat from dataset) as t
WHERE t.id = dataset.id;
```

#### Aditional queries

- Search by date interval
```sql
SELECT * FROM dataset WHERE date_time::date >= date '2008-02-02' AND date_time::date < date '2008-02-03';
``` 

</details>
