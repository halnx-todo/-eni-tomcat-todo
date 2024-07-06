# This is a Dockerfile for pedagogical purpose
# It can be use to build the docker image manually :
#
# docker volume create maven
# docker build -v /tmp/.m2 -t eni-todo-tomcat:latest --target eni-todo-tomcat-latest . &&\
# docker build -t eni-todo-tomcat-mariadb:latest --target eni-todo-tomcat-mariadb-latest . &&\
# docker build -t eni-todo:latest --target eni-todo .

FROM maven:3.9-eclipse-temurin-21-jammy AS builder
ARG SKIP_TESTS="true"
### pour la version tomcat-mariadb-harcoded-war
ARG MULTIPART_LOCATION=/usr/local/tomcat/files
ARG DB_DTB_JDBC_URL='jdbc:mariadb://127.0.0.1:3306/db_todo'
ARG DB_DTB_USERNAME="springuser"
ARG DB_DTB_PASSWORD="mypassword-quoor-uHoe7z"


COPY pom.xml .
RUN  mvn --batch-mode dependency:copy-dependencies dependency:copy-dependencies dependency:go-offline
#COPY src/maim/java/net/diehard/sample/todowebsite/Application.java ./src/maim/java/net/diehard/sample/todowebsite/Application.java
#RUN  --mount=type=cache,target=/tmp/.m2 mvn --batch-mode package -P simpleapp -Dmaven.repo.local=/tmp/.m2 -DskipTests=${SKIP_TESTS}
#RUN  --mount=type=cache,target=/tmp/.m2 mvn --batch-mode package -P tomcat-h2 -Dmaven.repo.local=/tmp/.m2 -DskipTests=${SKIP_TESTS}

COPY src ./src

RUN  --mount=type=cache,target=/var/cache/m2 mvn --batch-mode package -P simpleapp -Dmaven.repo.local=/var/cache/m2 -DskipTests=${SKIP_TESTS}
RUN  --mount=type=cache,target=/var/cache/m2 mvn --batch-mode package -P tomcat-h2 -Dmaven.repo.local=/var/cache/m2 -DskipTests=${SKIP_TESTS}

RUN  --mount=type=cache,target=/var/cache/m2 mvn --batch-mode package -P boot-mariadb -Dmaven.repo.local=/var/cache/m2 -DskipTests=${SKIP_TESTS}
RUN  --mount=type=cache,target=/var/cache/m2 mvn --batch-mode package -P tomcat-mariadb \
                              -Dmultipartlocation=${MULTIPART_LOCATION} \
                              -Dmariajdbcurl="${DB_DTB_JDBC_URL}" \
                              -Dmariadbusername=${DB_DTB_USERNAME} \
                              -Dmariadbpassword=${DB_DTB_PASSWORD} \
                              -Dmaven.repo.local=/var/cache/m2 -DskipTests=${SKIP_TESTS}
#RUN  mvn --batch-mode package -P tomcat-mariadb -Dmaven.repo.local=/var/cache/m2 -DskipTests=${SKIP_TESTS}

####### eni-todo-tomcat-base #######
FROM tomcat:10.1-jre21-temurin-jammy AS eni-todo-tomcat-base

ARG MULTIPART_LOCATION=/usr/local/tomcat/files

LABEL vendor=ACME\ Incorporated \
      net.diehard.sample.todowebsite.version="XXXX"
ARG MULTIPART_LOCATION=/usr/local/tomcat/files
RUN mkdir -p ${MULTIPART_LOCATION}

ENV SPRING_PROFILES_ACTIVE="default"

ENV CATALINA_OPTS="${CATALINA_OPTS} -XshowSettings:vm"
ENV CATALINA_OUT=/dev/null


ADD src/main/conf/server-tomcat-mariadb.xml /usr/local/tomcat/conf/server.xml
ADD src/main/conf/context-tomcat-mariadb.xml /usr/local/tomcat/conf/context.xml


####### eni-todo-tomcat-h2-env #######
FROM eni-todo-tomcat-base AS eni-todo-tomcat-h2-env
COPY --from=builder  /target/eni-todo-tomcat-h2.war /usr/local/tomcat/webapps/eni-todo.war

####### eni-todo-tomcat-mariadb-harcoded #######
FROM eni-todo-tomcat-base AS eni-todo-tomcat-mariadb-harcoded
ENV SPRING_PROFILES_ACTIVE=tomcat-mariadb-harcoded
# --chown=1000:0
COPY --from=builder  /target/eni-todo-tomcat-mariadb.war /usr/local/tomcat/webapps/eni-todo.war

####### eni-todo-tomcat-mariadb-env #######

FROM eni-todo-tomcat-base AS eni-todo-tomcat-mariadb-env
ENV SPRING_PROFILES_ACTIVE=tomcat-mariadb-env

ADD src/main/conf/catalina.properties            /usr/local/tomcat/conf/catalina.properties
ADD src/main/conf/logging.properties             /usr/local/tomcat/conf/logging.properties
# ADD src/test/jmeter/web-tomcat-mariadb-debug.xml /usr/local/tomcat/conf/web.xml

COPY --from=builder  /target/eni-todo-tomcat-mariadb.war /usr/local/tomcat/webapps/eni-todo.war

####### eni-todo-tomcat-mariadb-kub #######

FROM eni-todo-tomcat-base AS eni-todo-tomcat-mariadb-kub
ENV SPRING_PROFILES_ACTIVE=tomcat-mariadb-kub

ADD src/main/conf/catalina.properties /usr/local/tomcat/conf/catalina.properties
ADD src/main/conf/logging.properties /usr/local/tomcat/conf/logging.properties
ADD src/main/conf/server-tomcat-mariadb-kub.xml /usr/local/tomcat/conf/server.xml
ADD src/main/conf/context-tomcat-mariadb-kub.xml /usr/local/tomcat/conf/context.xml
COPY --from=builder  /target/eni-todo-tomcat-mariadb.war /usr/local/tomcat/webapps/eni-todo.war

####### eni-todo-boot-base #######
FROM  eclipse-temurin:21-jre-jammy AS eni-todo-boot-base

ARG MULTIPART_LOCATION=/usr/local/eni-todo/files

ENV SPRING_PROFILES_ACTIVE="default"

LABEL vendor=ACME\ Incorporated \
      net.diehard.sample.todowebsite.version="XXXX"
ENV SPRING_PROFILES_ACTIVE=boot-h2-env

ENV JAVA_OPTIONS=" -XshowSettings:vm "

RUN mkdir -p /usr/local/eni-todo &&\
     cd /usr/local/eni-todo &&\
     chown -R 1000:0 /usr/local/eni-todo &&\
     mkdir -p ${MULTIPART_LOCATION} &&\
     chown -R 1000:0 ${MULTIPART_LOCATION}

WORKDIR /usr/local/eni-todo/
USER 1000
CMD java ${JAVA_OPTIONS} -jar eni-todo.jar

####### eni-todo-boot-h2-env #######
FROM eni-todo-boot-base AS eni-todo-boot-h2-env
ENV SPRING_PROFILES_ACTIVE=boot-h2-env
COPY --from=builder --chown=1000:0  /target/eni-todo.jar /usr/local/eni-todo/eni-todo.jar


####### eni-todo-boot-mariadb-env #######
FROM eni-todo-boot-base AS eni-todo-boot-mariadb-env
ENV SPRING_PROFILES_ACTIVE=boot-mariadb-env
COPY --from=builder --chown=1000:0 /target/eni-todo-boot-mariadb.jar /usr/local/eni-todo/eni-todo.jar

####### eni-todo-boot-mariadb-kube #######
#@TODO