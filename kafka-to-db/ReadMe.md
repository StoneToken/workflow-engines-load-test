# Импорт метрик из Кафка в базу данных PostgreSQL

Настройка подключений к кафка и БД осуществляется в [application.yml](src/main/resources/application.yml)

```yaml
database:
  driverClassName: org.postgresql.Driver
  jdbcUrl: jdbc:postgresql://127.0.0.1:5432/dbname
  username: user
  password: password
  schema: kafka
```
password - шифруется


## Grafana
[http://10.31.0.5:30869/d/pIfb67Qnz/kogito-metrics?orgId=1](http://10.31.0.5:30869/d/pIfb67Qnz/kogito-metrics?orgId=1)

### Запросы

#### Процессов стартовано
```postgresql
select to_char(p.starttime, 'YYYY-MM-DD HH24:MI:SS')::timestamp as time,
       p.processName,       
       count(1) as " "
from ProcessInstance p
where p.starttime between (timestamp $__timeFrom()) and (timestamp $__timeTo())
group by to_char(p.starttime, 'YYYY-MM-DD HH24:MI:SS'), p.processName
order by 1,2
```

#### Процессов завершено
```postgresql
select to_char(p.endtime, 'YYYY-MM-DD HH24:MI:SS')::timestamp as time,
       p.processName,       
       count(1) as " "
from ProcessInstance p
where p.endtime between (timestamp $__timeFrom()) and (timestamp $__timeTo())
group by to_char(p.endtime, 'YYYY-MM-DD HH24:MI:SS'), p.processName
order by 1,2
```

#### Длительность выполнения процесса (ms)
```postgresql
select to_char(p.starttime, 'YYYY-MM-DD HH24:MI:SS')::timestamp as time,
       p.processName,       
       avg((EXTRACT('epoch' from p.endtime) - EXTRACT('epoch' from p.starttime)) * 1000) as " "
from ProcessInstance p
where p.endtime is not null 
and p.starttime between (timestamp $__timeFrom()) and (timestamp $__timeTo())
group by to_char(p.starttime, 'YYYY-MM-DD HH24:MI:SS'), p.processName
order by 1,2
```

#### Завершено шагов в секунду
```postgresql
select to_char(n.endtime, 'YYYY-MM-DD HH24:MI:SS')::timestamp as time,
       p.processName,
       count(1) as " "
from ProcessInstance p
join NodeInstance n on n.processinstanceid = p.id and n.endtime is not null
where n.endtime between (timestamp $__timeFrom()) and (timestamp $__timeTo())
group by to_char(n.endtime, 'YYYY-MM-DD HH24:MI:SS'), p.processName
order by 1,2
```

#### Ошибки
```postgresql
select to_char(p.endtime, 'YYYY-MM-DD HH24:MI:SS')::timestamp as time,
       p.processName,       
       count(1) as " "
from ProcessInstance p
where p.error is not null
and p.endtime between (timestamp $__timeFrom()) and (timestamp $__timeTo())
group by to_char(p.endtime, 'YYYY-MM-DD HH24:MI:SS'), p.processName
order by 1,2
```

#### Процессы Pass/Failure rate
```postgresql
select
    t.time as time,
    p1.processName,
    count(p1.processName) * 100 / count(p2.processName) as " "
from (select generate_series as time from pg_catalog.generate_series(
        date_trunc('minute', timestamp $__timeFrom()),
        date_trunc('minute', timestamp $__timeTo()), interval '1 second')) t
join ProcessInstance p1 on to_char(p1.starttime, 'YYYY-MM-DD HH24:MI:SS') = to_char(t.time, 'YYYY-MM-DD HH24:MI:SS') and p1.state = 2
join ProcessInstance p2 on to_char(p2.starttime, 'YYYY-MM-DD HH24:MI:SS') = to_char(t.time, 'YYYY-MM-DD HH24:MI:SS') and p2.processName = p1.processName
group by t.time, p1.processName
order by 1,2
```

# Создание образа

```text
docker login registry.sigma.sbrf.ru

docker build -t kafka-to-db:1.0.1 .
```

```text
docker login dzo.sw.sbc.space

docker tag kafka-to-db:1.0.1 dzo.sw.sbc.space/sbt_dev/ci90000011_bpmx_dev/kafka_to_db:1.0.1

docker push dzo.sw.sbc.space/sbt_dev/ci90000011_bpmx_dev/kafka_to_db:1.0.1
```