#!/bin/sh

#java -Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=8079,suspend=n -Djava.security.egd=file:/dev/./urandom -jar /app/expenseManager.jar

java -Xdebug -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=8790 -Djava.security.egd=file:/dev/./urandom -jar /app/expenseManager.jar
