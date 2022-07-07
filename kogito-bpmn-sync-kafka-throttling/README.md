# Example of problem "the current thread can not be blocked"

### Prerequisites

You should map domain name 'host.docker.internal' to 127.0.0.1
```shell
sudo echo $'\n127.0.0.1 host.docker.internal' >> /etc/hosts
```

You will need:
- Java 11+ installed
- Environment variable JAVA_HOME set accordingly
- Maven 3.8.1+ installed

When using native image compilation, you will also need:
- GraalVM 19.1+ installed
- Environment variable GRAALVM_HOME set accordingly
- Note that GraalVM native image compilation typically requires other packages (glibc-devel, zlib-devel and gcc) to be installed too, please refer to GraalVM installation documentation for more details.



## Reproducing

1. Start up environment from docker-compose folder
```sh
cd docker-compose
docker-compose up -d
```
2. Build and start project
```sh
mvn clean package quarkus:dev
```
3. After process started, pass load
```sh
cd ../
./lt_script_kafka_message_start.sh
```
It uses 100 nonstop messages, where each message start process with 7 rest task. Each rest request waited for a 1 second.
So, each process execution takes about 7-8 seconds. As soon as all the worker threads are busy, the event loop thread will start executing the rest request and an exception will be thrown:
```text
2022-07-28 15:57:56,483 ERROR [org.jbp.wor.ins.imp.WorkflowProcessInstanceImpl] (vert.x-eventloop-thread-23) Unexpected error while executing node SrvTaskRest1 in process instance 5249f6ee-28e1-4a4c-9e54-c197bb79a50f: org.jbpm.workflow.instance.WorkflowRuntimeException: [sequentialServiceTask:5249f6ee-28e1-4a4c-9e54-c197bb79a50f - SrvTaskRest1:8] -- The current thread cannot be blocked: vert.x-eventloop-thread-23
	at org.jbpm.workflow.instance.node.WorkItemNodeInstance.getExceptionScopeInstance(WorkItemNodeInstance.java:205)
	at org.jbpm.workflow.instance.node.WorkItemNodeInstance.handleException(WorkItemNodeInstance.java:199)
	at org.jbpm.workflow.instance.node.WorkItemNodeInstance.processWorkItemHandler(WorkItemNodeInstance.java:189)
	at org.jbpm.workflow.instance.node.WorkItemNodeInstance.internalTrigger(WorkItemNodeInstance.java:161)
	at org.jbpm.workflow.instance.impl.NodeInstanceImpl.trigger(NodeInstanceImpl.java:225)
	...
```

But, if enable throttling:
```properties
kogito.quarkus.events.throttling.enabled=true
```
the process will handle messages correctly.