FROM openjdk:11
LABEL maintainer="shibkov.k@gmail.com"
VOLUME /tmp
EXPOSE 8080
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} sjc-tasks-checker-0.0.1-SNAPSHOT.jar
ENTRYPOINT ["java","-Dspring.profiles.active=dev","-jar","sjc-tasks-checker-0.0.1-SNAPSHOT.jar"]