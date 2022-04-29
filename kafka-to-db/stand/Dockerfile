FROM registry.sigma.sbrf.ru/base/redhat/redhat-openjdk-18/openjdk18-openshift:latest

ENV APP_HOME /home/jboss

ENV JAVA_OPTS=""
ENV EXTRA_JAVA_OPTS=""

WORKDIR $APP_HOME

COPY target/kafka-to-db-*.jar $APP_HOME/app.jar

CMD ["sh", "-c","java $JAVA_OPTS $EXTRA_JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar $APP_HOME/app.jar"]