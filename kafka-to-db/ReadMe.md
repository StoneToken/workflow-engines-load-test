# Импорт метрик из Кафка в базу данных PostgreSQL

Настройка подключений к кафка и БД осуществляется в [application.yml](src/main/resources/application.yml)

```yaml
kafka:

  processinstances-events:
    bootstrap-servers: 127.0.0.0:31543
    eventsTopic: kogito-processinstances-events
    listener.concurency: 4
    consumer.groupId: kogito_to_db

  jobs-events:
    bootstrap-servers: 127.0.0.0:31543
    eventsTopic: kogito-jobs-events
    listener.concurency: 4
    consumer.groupId: kogito_to_db

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
select to_char(p.startTime, 'YYYY-MM-DD HH24:MI:SS')::timestamp as time,
       p.processName,       
       count(1) as " "
from ProcessInstance p
where p.error is not null
and p.startTime between (timestamp $__timeFrom()) and (timestamp $__timeTo())
group by to_char(p.startTime, 'YYYY-MM-DD HH24:MI:SS'), p.processName
order by 1,2
```

#### Процессы Pass/Failure rate
```postgresql
select timestamp $__timeFrom() as time, 
p.processName,
(select count(1) as cnt from ProcessInstance p2 where p2.processname = p.processname and p2.starttime between timestamp $__timeFrom() and timestamp $__timeTo() and p2.state = 2) * 100.00 /
(select count(1) as cnt from ProcessInstance p1 where p1.processname = p.processname and p1.starttime between timestamp $__timeFrom() and timestamp $__timeTo())
from ProcessInstance p
where p.starttime between timestamp $__timeFrom() and timestamp $__timeTo()
group by p.processname
```

#### Jobs
```postgresql
select to_char(j.time, 'YYYY-MM-DD HH24:MI:SS')::timestamp as time,
       p.processName || ' ' || j.status as status,
       count(1) as " "
from Jobs j
join ProcessInstance p on p.id = j.ProcessInstanceId
where j.time between (timestamp $__timeFrom()) and (timestamp $__timeTo())
group by to_char(j.time, 'YYYY-MM-DD HH24:MI:SS'), p.processName, j.status
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

# Очистка данных в БД

- Удалить PODs **kafka-to-db-kogito**
- Удалить таблицы
```postgresql
drop table if exists processinstance;
drop table if exists nodeinstance;
drop table if exists jobs;
```
- Поднять PODs **kafka-to-db-kogito**
