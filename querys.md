# Querys

### Add column for geometry
alter table taxi_data add column geom geometry;

### Create table with taxis inside the osm
create table taxi_data_osm (
 id serial primary key,  
 taxi_id integer, 
 datetime timestamp,
 longitude double precision,
 latitude double precision
);

### Add column for id in taxi_data
alter table taxi_data add column id serial primary key;

### Create geometrys for cada taxi position
update taxi_data
set geom = ST_SetSRID(ST_MakePoint(t.long, t.lat), 4326)
from (
       select id, longitude as long, 
         latitude as lat from taxi_data) as t
WHERE t.id = taxi_data.id;


### 
