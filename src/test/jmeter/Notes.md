# jmeter

## to record 

Firefox by default does not allow localhost or 127.0.0.1 to be proxied.
You have to modify a setting in about:config
change network.proxy.allow_hijacking_localhost to true


## run without GUI

```shell
TIMESLOT=$(date +%y%m%d%H%M%S) &&\
   mkdir -p target/jmeter/repport/${TIMESLOT} &&\
   podman run --name=24.04-tomcat-h2-env-${TIMESLOT} --rm \
     -d -p 8080:8080 localhost/tomcat-eni-todo:v24.04-tomcat-h2-env &&\
   echo "time : ${TIMESLOT}" &&\
   sleep 180 &&\
   jmeter -n -t src/test/jmeter/eni-todo-dynamic.jmx \
      -JthreadActive=true \
      -j target/jmeter/jmeter-run-${TIMESLOT}.log \
      -l target/jmeter/jmeter-${TIMESLOT}.csv -e -o target/jmeter/repport/${TIMESLOT}/ &\
   sleep 250 &&\
   shutdown.sh &&\ 
   podman stop 24.04-tomcat-h2-env-${TIMESLOT}
```

to access the repport
```shell
open target/jmeter/repport/$TIMESLOT/index.html
```
## build with podman

```shell
```bash
MULTIPART_LOCATION=/usr/local/tomcat/files &&\
DB_DTB_JDBC_URL="jdbc:mariadb://mariadb:3306/db_todo" &&\
DB_DTB_PASSWORD="mypassword-quoor-uHoe7z" &&\
DB_DTB_USERNAME="springuser" &&\
podman build -t tomcat-eni-todo:v24.04-tomcat-mariadb-harcoded \
   --build-arg MULTIPART_LOCATION="${MULTIPART_LOCATION}" \
   --build-arg DB_DTB_JDBC_URL="${DB_DTB_JDBC_URL}" \
   --build-arg DB_DTB_USERNAME="${DB_DTB_USERNAME}" \
   --build-arg DB_DTB_PASSWORD="${DB_DTB_USERNAME}" \
   --target eni-todo-tomcat-mariadb-harcoded . &&\
podman build -t tomcat-eni-todo:v24.04-tomcat-h2-env --target eni-todo-tomcat-h2-env . &&\
podman build -t tomcat-eni-todo:v24.04-tomcat-mariadb-env --target eni-todo-tomcat-mariadb-env . &&\
podman build -t tomcat-eni-todo:v24.04-tomcat-mariadb-kub --target eni-todo-tomcat-mariadb-kub . 
```



## Full test

get my IP
```shell
MY_MACHINE_IP=$(ipconfig getifaddr en0)
```

run mariadb in daemon mode

```shell
DB_DTB_PASSWORD="mypassword-quoor-uHoe7z" &&\
DB_DTB_USERNAME="springuser" &&\
DB_DTB_PORT="3306" &&\
DB_DTB_NAME="db_todo" &&\
DB_DTB_ROOT_PASSWORD="r00t-aeKie8ahWai_"
podman run --rm \
  -e MYSQL_USER="${DB_DTB_USERNAME}" \
  -e MYSQL_PASSWORD="${DB_DTB_PASSWORD}" \
  -e MYSQL_DATABASE="${DB_DTB_NAME}" \
  -e MYSQL_ROOT_PASSWORD="${DB_DTB_USERNAME}" \
  -p ${DB_DTB_PORT}:${DB_DTB_PORT} \
  -d mariadb:11.2.2
```

run eni-todo 
```shell
MY_MACHINE_IP=$(ipconfig getifaddr en0) &&\
DB_DTB_PASSWORD="mypassword-quoor-uHoe7z" &&\
DB_DTB_USERNAME="springuser" &&\
SPRING_PROFILES_ACTIVE=tomcat-mariadb-env &&\
DB_DTB_JDBC_URL="jdbc:mariadb://${MY_MACHINE_IP}:${DB_DTB_PORT}/db_todo" &&\
MULTIPART_LOCATION=/usr/local/tomcat/files
podman run --rm -p 8080:8080 -e SPRING_PROFILES_ACTIVE="${SPRING_PROFILES_ACTIVE}" \
                             -e MULTIPART_LOCATION="${MULTIPART_LOCATION}" \
                             -e DB_DTB_JDBC_URL="${DB_DTB_JDBC_URL}" \
                             -e DB_DTB_USERNAME="${DB_DTB_USERNAME}" \
                             -e DB_DTB_PASSWORD="${DB_DTB_PASSWORD}" \
                             tomcat-eni-todo:24.04-tomcat-mariadb-env
```

## save

```shell
mkdir -p target/docker-save;
podman save docker.io/library/mariadb:11.2.2 -o target/docker-save/mariadb--10.5.8-arm64.tar;

podman save localhost/tomcat-eni-todo:v24.04-tomcat-mariadb-kub  -o target/docker-save/tomcat-eni-todo--24.04-tomcat-mariadb-kub-arm64.tar;
podman save localhost/tomcat-eni-todo:v24.04-tomcat-mariadb-env  -o target/docker-save/tomcat-eni-todo--24.04-tomcat-mariadb-env-arm64.tar;
podman save localhost/tomcat-eni-todo:v24.04-tomcat-h2-env       -o target/docker-save/tomcat-eni-todo--24.04-tomcat-h2-env-arm64.tar;
```

## for debug purpose

in your target `application.properties`
```properties
debug=true
# or
trace=true
```

in `/usr/local/tomcat/conf/web.xml` (example src/test/jmeter/web-tomcat-mariadb-debug.xml) 

```xml
<filter>
    <filter-name>requestDumper</filter-name>
    <filter-class>org.apache.catalina.filters.RequestDumperFilter</filter-class>
</filter>
<filter-mapping>
    <filter-name>requestDumper</filter-name>
    <url-pattern>*</url-pattern>
</filter-mapping>
```

```properties
## for debugging tomcat
#1request-dumper.org.apache.juli.FileHandler.level = FINE
#1request-dumper.org.apache.juli.FileHandler.directory = ${catalina.base}/logs
#1request-dumper.org.apache.juli.FileHandler.prefix = request-dumper.
#1request-dumper.org.apache.juli.FileHandler.formatter = org.apache.juli.VerbatimFormatter
#org.apache.catalina.filters.RequestDumperFilter.level = FINE
#org.apache.catalina.filters.RequestDumperFilter.handlers = 1request-dumper.org.apache.juli.FileHandler

```