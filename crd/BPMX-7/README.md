### Перепулить docker images
Команда для push image
```shell
docker pull <image>
docker tag <image> <registry>/<path>/<image>
docker push <registry>/<path>/<image>
```

### Краткий порядок развертывания kogito

1. Убедиться что есть секрет на выгрузку из репы (dzo_secret.yaml + flow-pull-secret.yaml)

2. Установить необходимые операторы kogito, strimzi, infinispan, keycloak, grafana, prometheus по [доке kogito operator](https://docs.jboss.org/kogito/release/1.19.0/html_single/#con-kogito-operator-installation_kogito-deploying-on-openshift).
Обратить внимание, операторы должны быть установлены в одном namespace.
   
3. Развернуть операторами сопутствующую инфру: keycloak, kafka, infinispan, grafana, prometheus.

4. Далее можно следовать по инструкции kogito оператора [доке kogito operator](https://docs.jboss.org/kogito/release/1.19.0/html_single/#con-kogito-operator-installation_kogito-deploying-on-openshift).