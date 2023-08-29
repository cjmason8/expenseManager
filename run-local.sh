#!/bin/bash

AUTH_SERVICE_END_POINT=http://localhost:8082
DB_DRIVER=org.postgresql.Driver
DB_PASS=postgres
DB_URL=jdbc:postgresql://localhost:5444/expensemanager
DB_USER=postgres
#HIBERNATE_DIALECT=org.hibernate.dialect.PostgreSQLDialect
HIBERNATE_DIALECT=au.com.mason.expensemanager.repository.MyPostgreSQL94Dialect
HIBERNATE_HBM2DDL_AUTO=update
ALPHA_VEC=E66YYu84iW50GE66
RESOURCE_DIR=/resources
REQ_ACCOUNT=icMZGRCb+RbYUKI5RX7HaM33C3mLyertwbUl2RhYdt8=  

export DB_DRIVER
export DB_PASS
export DB_URL
export DB_USER
export HIBERNATE_DIALECT
export HIBERNATE_HBM2DDL_AUTO
export AUTH_SERVICE_END_POINT
export ALPHA_VEC
export RESOURCE_DIR
export REQ_ACCOUNT

mvn clean install
mvn -Dserver.port=8083 spring-boot:run
