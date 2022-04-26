# Импорт метрик из Кафка в базу данных PostgtreSQL

Настройка подключений задается в application.yml

```yaml
database:
  driverClassName: org.postgresql.Driver
  jdbcUrl: jdbc:postgresql://localhost:5433/bdengine
  username: user
  password: QTQ4Ql57fVUtNTxMSHZnPQ==
```
password - шифруется


## Grafana
http://10.31.0.5:30869/d/pIfb67Qnz/kogito-metrics?orgId=1
http://srv-0-132.pc.dev.sbt:3000/d/pIfb67Qnz/kogito-metrics?orgId=1
