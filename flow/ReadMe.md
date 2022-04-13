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

## Отчет
|Пространство|Ссылка|
| :--- | :--- |
|Sigma|https://confluence.sberbank.ru/pages/viewpage.action?pageId=7349469257|
|DZO|https://dzo.sw.sbc.space/wiki/pages/viewpage.action?pageId=34254946|

### Сравнительная таблица
Все показатели, полученные в ходе тестирования, характерны для модели процесса и профиля используемых в данном тесте.  

#### Поиск и подтверждение максимума на модели с шагами StartEvent и StopEvent
<table><tbody>
<tr><th>Время теста<br>(длительность)</th><th>Версия</th><th>Процесс</th><th>Кол-во<br>Pods</th><th>Отправлено<br>запросов</th><th>COMPETED</th><th>RUNNING</th><th>FAILED</th><th>Pass/Failure<br>rate</th><th>потеряно</th><th>общее<br>количество</th><th>Шаги, завершено<br>в секунду<br>mean</th><th>Шаги, завершено<br>в секунду<br>90%</th><th>Шаги, завершено<br>в секунду<br>max</th><th>max tps<br>(VU)</th><th>Response<br>time<br>90% (мс)</th><th>Длительность<br>выполнения<br>90% (мс)</th></tr>
<tr><td>07-04-2022 13:19:43<br>07-04-2022 14:29:52<br>(01:10:09)</td><td>4.8.130 Kubernetes	SberCloud (DZO)</td><td>TestCase_StartStop<br>load_DZO.properties<br>Количество процессов на начало теста 4 443 057<br>БД master: 10.31.0.130:5433/bdengine (16cpu, RAM 128GB, HDD 500GB)<br>VU(max)=558</td><td>4</td><td>1 733 068</td><td class="td_green">1 733 068</td><td>0</td><td>0</td><td>100</td><td>0</td><td>3 466 136</td><td>824</td><td>1 158</td><td>1 798</td><td>414,849<br>558</td><td>1 988</td><td>6 213</td></tr>
<tr><td>07-04-2022 12:48:11<br>07-04-2022 13:56:21<br>(01:08:10)</td><td>4.8.133 OpenShift Alpha [ARSLAB]</td><td>TestCase_StartStop<br>load_alpha_lt1.properties<br>Engine 4PODs 2cpu, 1cpu istio<br>Количество процессов на начало теста 292 384<br>БД master: 10.116.176.158:5433/engine (16cpu, RAM 128GB, HDD 100GB)<br>VU(max)=462</td><td>4</td><td>1 810 503</td><td class="td_green">1 810 503</td><td>0</td><td>0</td><td>100</td><td>0</td><td>3 621 006</td><td>887</td><td>993</td><td>1 215</td><td>457,803<br>462</td><td>274</td><td>513</td></tr>
</tbody></table>

#### Поиск и подтверждение максимума на модели с одним RestTask
<table><tbody>
<tr><th>Время теста<br>(длительность)</th><th>Версия</th><th>Процесс</th><th>Кол-во<br>Pods</th><th>Отправлено<br>запросов</th><th>COMPETED</th><th>RUNNING</th><th>FAILED</th><th>Pass/Failure<br>rate</th><th>потеряно</th><th>общее<br>количество</th><th>Шаги, завершено<br>в секунду<br>mean</th><th>Шаги, завершено<br>в секунду<br>90%</th><th>Шаги, завершено<br>в секунду<br>max</th><th>max tps<br>(VU)</th><th>Response<br>time<br>90% (мс)</th><th>Длительность<br>выполнения<br>90% (мс)</th></tr>
<tr><td>08-04-2022 07:35:26<br>08-04-2022 08:43:06<br>(01:07:40)</td><td>4.8.130 Kubernetes	SberCloud (DZO)</td><td>TestCase_RestTask1<br>load_DZO.properties<br>Количество процессов на начало теста 7 551 483<br>БД master: 10.31.0.130:5433/bdengine (16cpu, RAM 128GB, HDD 500GB)<br>VU(max)=439</td><td>4</td><td>817 291</td><td class="td_green">817 291</td><td>0</td><td>0</td><td>100</td><td>0</td><td>2 451 129</td><td>604</td><td>1 274</td><td>1 882</td><td>193,12<br>439</td><td>2 957</td><td>11 682</td></tr>
<tr><td>08-04-2022 07:18:38<br>08-04-2022 08:24:49<br>(01:06:11)</td><td>4.8.133 OpenShift Alpha [ARSLAB]</td><td>TestCase_RestTask1<br>load_alpha_lt1.properties<br>Engine 4PODs 2cpu, 1cpu istio<br>Количество процессов на начало теста 3 239 967<br>БД master: 10.116.176.158:5433/engine (16cpu, RAM 128GB, HDD 100GB)<br>VU(max)=367</td><td>4</td><td>1 181 246</td><td class="td_green">1 181 246</td><td>0</td><td>0</td><td>100</td><td>0</td><td>3 543 736</td><td>894</td><td>1 030</td><td>1 286</td><td>304,803<br>367</td><td>343</td><td>1 105</td></tr>
</tbody></table>

#### Поиск и подтверждение максимума на модели с семью последовательно выполняющимися RestTask
<table><tbody>
<tr><th>Время теста<br>(длительность)</th><th>Версия</th><th>Процесс</th><th>Кол-во<br>Pods</th><th>Отправлено<br>запросов</th><th>COMPETED</th><th>RUNNING</th><th>FAILED</th><th>Pass/Failure<br>rate</th><th>потеряно</th><th>общее<br>количество</th><th>Шаги, завершено<br>в секунду<br>mean</th><th>Шаги, завершено<br>в секунду<br>90%</th><th>Шаги, завершено<br>в секунду<br>max</th><th>max tps<br>(VU)</th><th>Response<br>time<br>90% (мс)</th><th>Длительность<br>выполнения<br>90% (мс)</th></tr>
<tr><td>06-04-2022 18:53:21<br>06-04-2022 20:03:03<br>(01:09:42)</td><td>4.8.130 Kubernetes	SberCloud (DZO)</td><td>TestCase_RestTask7_Sequential<br>load_DZO.properties<br>Количество процессов на начало теста 1 107 610<br>БД master: 10.31.0.130:5433/bdengine (16cpu, RAM 128GB, HDD 500GB)<br>VU(max)=534</td><td>4</td><td>714 484</td><td class="td_green">714 484</td><td>0</td><td>0</td><td>100</td><td>0</td><td>6 430 356</td><td>1 539</td><td>1 839</td><td>2 059</td><td>178,056<br>534</td><td>121</td><td>2 896</td></tr>
<tr><td>07-04-2022 14:24:04<br>07-04-2022 15:31:16<br>(01:07:12)</td><td>4.8.133 OpenShift Alpha [ARSLAB]</td><td>TestCase_RestTask7_Sequential<br>load_alpha_lt1.properties<br>Engine 4PODs 2cpu, 1cpu istio<br>Количество процессов на начало теста 2 102 805<br>БД master: 10.116.176.158:5433/engine (16cpu, RAM 128GB, HDD 100GB)<br>VU(max)=413</td><td>4</td><td>539 236</td><td class="td_green">539 236</td><td>0</td><td>0</td><td>100</td><td>0</td><td>4 853 124</td><td>1 206</td><td>1 377</td><td>1 592</td><td>137,637<br>413</td><td>244</td><td>8 919</td></tr>
</tbody></table>

#### Поиск и подтверждение максимума на модели с семью параллельно выполняющимися RestTask
<table><tbody>
<tr><th>Время теста<br>(длительность)</th><th>Версия</th><th>Процесс</th><th>Кол-во<br>Pods</th><th>Отправлено<br>запросов</th><th>COMPETED</th><th>RUNNING</th><th>FAILED</th><th>Pass/Failure<br>rate</th><th>потеряно</th><th>общее<br>количество</th><th>Шаги, завершено<br>в секунду<br>mean</th><th>Шаги, завершено<br>в секунду<br>90%</th><th>Шаги, завершено<br>в секунду<br>max</th><th>max tps<br>(VU)</th><th>Response<br>time<br>90% (мс)</th><th>Длительность<br>выполнения<br>90% (мс)</th></tr>
<tr><td>07-04-2022 07:26:05<br>07-04-2022 08:32:17<br>(01:06:12)</td><td>4.8.130 Kubernetes	SberCloud (DZO)</td><td>TestCase_RestTask7_Parallel<br>load_DZO.properties<br>Количество процессов на начало теста 1 822 051<br>БД master: 10.31.0.130:5433/bdengine (16cpu, RAM 128GB, HDD 500GB)<br>VU(max)=326</td><td>4</td><td>419 156</td><td class="td_green">419 156</td><td>0</td><td>0</td><td>100</td><td>0</td><td>7 125 652</td><td>1 796</td><td>2 147</td><td>2 661</td><td>108,701<br>326</td><td>76</td><td>4 586</td></tr>
<tr><td>07-04-2022 15:42:35<br>07-04-2022 16:46:48<br>(01:04:13)</td><td>4.8.133 OpenShift Alpha [ARSLAB]</td><td>TestCase_RestTask7_Parallel<br>load_alpha_lt1.properties<br>Engine 4PODs 2cpu, 1cpu istio<br>Количество процессов на начало теста 2 640 985<br>БД master: 10.116.176.158:5433/engine (16cpu, RAM 128GB, HDD 100GB)<br>VU(max)=232</td><td>4</td><td>292 227</td><td class="td_green">292 227</td><td>0</td><td>0</td><td>100</td><td>0</td><td>4 482 756</td><td>1 165</td><td>1 384</td><td>1 722</td><td>77,336<br>232</td><td>180</td><td>2 032 588<br>(00:33:52.588)</td></tr>
</tbody></table>
