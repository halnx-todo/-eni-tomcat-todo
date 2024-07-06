# eni-tomcat-todo
Cette application est une application d'exemple pour le projet.

Elle permets de construire et d'executer un application web selon plusieurs mode:

Nous prenons quelque racourcit pour des raison pedagogique afin de mettre en avant les changements de pratique de 2005 a nos jours (2021)

Les images :
- eni-todo:24.04-tomcat-mariadb-harcoded : [application-tomcat-mariadb-harcoded.properties](src/main/resources/application-tomcat-mariadb-harcoded.properties)
  va utiliser le filtre maven pour modifier les parametres entre '@' avec les valeur passer lors du build.

- eni-todo:24.04-tomcat-h2-env : [application.properties](src/main/resources/application.properties)

  c'est la version tomcat avec une base h2 (memoire)

- eni-todo:24.04-tomcat-mariadb-env : [application-tomcat-mariadb-env.properties](src/main/resources/application-tomcat-mariadb-env.properties)
  
  c'est la version tomcat avec une base mariadb configurée à partir des variables d'environnement au lancement de l'image docker

- eni-todo:24.04-tomcat-mariadb-kub : [application-eni-todo:tomcat-mariadb-kub.properties](src/main/resources/application-tomcat-mariadb-kub.properties)
  
  c'est la version tomcat avec une base mariadb configurée à partir des variables d'environnement au lancement de l'image docker, le serveur est configurée pour utiliser un moteur de replication de session. Il est fait pour fonctionner dans Kubernetes
  
- eni-todo:24.04-boot-h2-env : [application-eni-todo:boot-h2-env.properties](src/main/resources/application-boot-h2-env.properties)
  
  c'est la version string-boot avec une base h2 (memoire)

- eni-todo:boot-mariadb-env : [application-eni-todo:boot-mariadb-env.properties](src/main/resources/application-boot-mariadb-env.properties)
  
  c'est la version string-boot avec une base mariadb paramètre par les variables d'environnement


## start mariadb

```bash
DB_DTB_PASSWORD="mypassword-quoor-uHoe7z" &&\
DB_DTB_USERNAME="springuser" &&\
DB_DTB_PORT="3306" &&\
DB_DTB_NAME="db_todo" &&\
DB_DTB_ROOT_PASSWORD="r00t-aeKie8ahWai_"
docker run --rm \
  -e MYSQL_USER="${DB_DTB_USERNAME}" \
  -e MYSQL_PASSWORD="${DB_DTB_PASSWORD}" \
  -e MYSQL_DATABASE="${DB_DTB_NAME}" \
  -e MYSQL_ROOT_PASSWORD="${DB_DTB_USERNAME}" \
  -p ${DB_DTB_PORT}:${DB_DTB_PORT} \
  -d mariadb:11.2.2
```

## Build all the images

```bash

MULTIPART_LOCATION=/usr/local/tomcat/files &&\
DB_DTB_JDBC_URL="jdbc:mariadb://mariadb:3306/db_todo" &&\
DB_DTB_PASSWORD="mypassword-quoor-uHoe7z" &&\
DB_DTB_USERNAME="springuser" &&\
docker build -t eni-todo:24.04-tomcat-mariadb-harcoded \
   --build-arg MULTIPART_LOCATION="${MULTIPART_LOCATION}" \
   --build-arg DB_DTB_JDBC_URL="${DB_DTB_JDBC_URL}" \
   --build-arg DB_DTB_USERNAME="${DB_DTB_USERNAME}" \
   --build-arg DB_DTB_PASSWORD="${DB_DTB_USERNAME}" \
   --target eni-todo-tomcat-mariadb-harcoded . &&\
docker build -t eni-todo:24.04-tomcat-h2-env --target eni-todo-tomcat-h2-env . &&\   
docker build -t eni-todo:24.04-tomcat-mariadb-env --target eni-todo-tomcat-mariadb-env . &&\
docker build -t eni-todo:24.04-tomcat-mariadb-kub --target eni-todo-tomcat-mariadb-kub . 
```

```bash

MULTIPART_LOCATION=/usr/local/tomcat/files &&\
DB_DTB_JDBC_URL="jdbc:mariadb://$(minikube -p minikube ip):3306/db_todo" &&\
DB_DTB_PASSWORD="mypassword-quoor-uHoe7z" &&\
DB_DTB_USERNAME="springuser" &&\
docker build -t eni-todo:24.04-tomcat-mariadb-harcoded \
   --build-arg MULTIPART_LOCATION="${MULTIPART_LOCATION}" \
   --build-arg DB_DTB_JDBC_URL="${DB_DTB_JDBC_URL}" \
   --build-arg DB_DTB_USERNAME="${DB_DTB_USERNAME}" \
   --build-arg DB_DTB_PASSWORD="${DB_DTB_USERNAME}" \
   --target eni-todo-tomcat-mariadb-harcoded . &&\
docker build -t eni-todo-tomcat-h2:latest --target eni-todo-tomcat-h2-env . &&\   
docker build -t eni-todo-tomcat-mariadb:latest --target eni-todo-tomcat-mariadb-env . &&\
docker build -t eni-todo:tomcat-mariadb-kub --target eni-todo-tomcat-mariadb-kub . 

```

## run tomcat-mariadb-harcoded

```bash
docker run --rm -p 8080:8080 eni-todo:24.04-tomcat-mariadb-harcoded
```


## run tomcat-mariadb-env with overridden values

ATTENION au minikube -p eni-todo ip pour avoir l'ip dynamiquement a partir de minikube

```bash
DB_DTB_JDBC_URL="jdbc:mariadb://$(minikube -p eni-todo ip):3306/db_todo" &&\
SPRING_PROFILES_ACTIVE=tomcat-mariadb-env &&\
DB_DTB_PASSWORD="mypassword-quoor-uHoe7z" &&\
DB_DTB_USERNAME="springuser" &&\
MULTIPART_LOCATION=/usr/local/tomcat/files 
docker run --rm -p 8080:8080 -e SPRING_PROFILES_ACTIVE="${SPRING_PROFILES_ACTIVE}" \
                             -e MULTIPART_LOCATION="${MULTIPART_LOCATION}" \
                             -e DB_DTB_JDBC_URL="${DB_DTB_JDBC_URL}" \
                             -e DB_DTB_USERNAME="${DB_DTB_USERNAME}" \
                             -e DB_DTB_PASSWORD="${DB_DTB_PASSWORD}" \
                             eni-todo:24.04-tomcat-mariadb-env
```

#.... INFO [main] org.apache.catalina.startup.Catalina.start Server startup in [312717] milliseconds
