# Querys

### Add column for geometry
```sql
alter table taxi_data add column geom geometry;
```

### Create table with taxis inside the osm
```sql
create table taxi_data_osm (
 id serial primary key,  
 taxi_id integer, 
 datetime timestamp,
 longitude double precision,
 latitude double precision
);
```
### Add column for id in taxi_data
```sql
alter table taxi_data add column id serial primary key;
```
### Create geometrys for cada taxi position
```sql
update taxi_data
set geom = ST_SetSRID(ST_MakePoint(t.long, t.lat), 4326)
from (
       select id, longitude as long, 
         latitude as lat from taxi_data) as t
WHERE t.id = taxi_data.id;

```
### Search by date interval
```sql
SELECT * FROM taxi_data WHERE date_time::date >= date '2008-02-02' AND date_time::date < date '2008-02-03';
``` 
