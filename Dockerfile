FROM maven:3.6-jdk-11 AS build
COPY src /usr/src/app/src  
COPY pom.xml /usr/src/app  
RUN mvn -f /usr/src/app/pom.xml clean install

FROM adoptopenjdk/openjdk11:x86_64-alpine-jdk-11.28

RUN adduser -D -u 1000 localUser

RUN mkdir /app

RUN chown localUser /app

COPY --from=build /usr/src/app/target/expensemanager-0.0.1-SNAPSHOT.jar /app/expenseManager.jar
COPY run.sh /app/run.sh

RUN chown localUser /app/expenseManager.jar
RUN chown localUser /app/run.sh

USER localUser

EXPOSE 8080
EXPOSE 8790

CMD ["/app/run.sh"]