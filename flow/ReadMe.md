# SberFlow-Engine load test

## Инструменты для подачи нагрузки
Для подачи нагрузки используется 
[Генератор нагрузки с построителем отчета](https://dzo.sw.sbc.space/bitbucket-ci/projects/BPMX/repos/load-manager/browse?at=refs%2Fheads%2F4G)

## Модели для проведения НТ
1. Модель с шагами StartEvent и StopEvent [c8c38b0b-166b-4354-8301-110eec32004c (TestCase_StartStop)](resources\models\md\TestCase_StartStop.md)
1. Модель с шагом Rest Task [b3a04993-3d9d-4333-980a-96025557c18a (TestCase_RestTask1)](resources\models\md\TestCase_RestTask1.md)
1. Модель с семью последовательно выполняющимися шагами Rest Task [d886f397-e962-46fc-bed2-8676c962b770 (TestCase_RestTask7_Sequential)](resources\models\md\TestCase_RestTask7_Sequential.md)
1. Модель с семью параллельно выполняющимися шагами Rest Task [deff117e-56cb-4068-b216-74d5b6e933d6 (TestCase_RestTask7_Parallel)](resources\models\md\TestCase_RestTask7_Parallel.md)

## Тестовый модуль (stub)
В шагах Rest Task осуществляется вызов API из
[тестового модуля](https://dzo.sw.sbc.space/bitbucket-ci/projects/BPMX/repos/sberflow-test/browse?at=refs%2Fheads%2Fdevelop)

## Тестирование
Тестирование проводится на стенде DZO в SberCloud.  
Запуск генератора нагрузки выполняется из 
[Jenkins](https://dzo.sw.sbc.space/jenkins-ci/job/bpmx/job/lt_engine)

Используются [профили](https://dzo.sw.sbc.space/bitbucket-ci/projects/BPMX/repos/load-manager/browse/Resources/TestPlans/TestPlans.json?at=refs%2Fheads%2F4G)

|Профиль|Наименование|
| :--- | :--- |
|StartStop (max)|Поиск и подтверждение максимума на модели с шагами StartEvent и StopEvent|
|4G RestTask1 (max)|Поиск и подтверждение максимума на модели с одним RestTask|
|4G RestTask7 Sequential (max)|Поиск и подтверждение максимума на модели с семью последовательно выполняющимися RestTask|
|4G RestTask7 Parallel (max)|Поиск и подтверждение максимума на модели с семью параллельно выполняющимися RestTask|

## Мониторинг
Мониторинг осуществляется при помощи Grafana

|Наименование дашборда|Ссылка|
| :--- | :--- |
|Основные метрики нагрузки (General/BPM SberFlow Engine)|http://srv-0-132.pc.dev.sbt:3000/d/RGZvERlWk/bpm-sberflow-engine?orgId=1&from=now-1h&to=now&refresh=5m|
|Утилизация БД (General/Dashboard_postgres_resources)|http://srv-0-132.pc.dev.sbt:3000/d/rYdddlPWk/dashboard_postgres_resources?orgId=1&refresh=1m&from=now-1h&to=now|
|Утилизация PODs (General/Kubernetes/Compute Resources/Pod)|http://srv-0-132.pc.dev.sbt:3000/d/6581e46e4e5c7ba40a07646395ef7b23/kubernetes-compute-resources-pod?orgId=1&var-datasource=Prometheus-1&var-cluster=&var-namespace=bpmx-lt&var-pod=sberflow-engine-k8s-lt-7c78c88c99-5b6hk&var-interval=4h&from=now-1h&to=now&refresh=1m|
|Метрики Engine (General/SberFlow-Engine metrics)|http://srv-0-132.pc.dev.sbt:3000/d/_tbFz0uMz/sberflow-engine-metrics?orgId=1&refresh=1m&var-datasource=Prometheus-engine&var-cluster=&var-namespace=bpmx-lt&from=now-1h&to=now|

