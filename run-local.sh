#!/bin/bash

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
if [ -f "${SCRIPT_DIR}/.env.local" ]; then
  set -a
  # shellcheck source=/dev/null
  source "${SCRIPT_DIR}/.env.local"
  set +a
else
  AUTH_SERVICE_END_POINT=http://localhost:8082
  DB_DRIVER=org.postgresql.Driver
  DB_PASS=postgres
  DB_URL=jdbc:postgresql://localhost:5444/expensemanager
  DB_USER=postgres
  HIBERNATE_DIALECT=org.hibernate.dialect.PostgreSQLDialect
  HIBERNATE_HBM2DDL_AUTO=update
  ALPHA_VEC=E66YYu84iW50GE66
  RESOURCE_DIR=/resources
  REQ_ACCOUNT=icMZGRCb+RbYUKI5RX7HaM33C3mLyertwbUl2RhYdt8=
  ENV=local
  LOG_DIR="${SCRIPT_DIR}/logs"
fi

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
export ENV="${ENV:-local}"
export LOG_DIR="${LOG_DIR:-${SCRIPT_DIR}/logs}"

mkdir -p "${LOG_DIR}"

mvn clean install
mvn -Dserver.port="${SERVER_PORT:-8083}" spring-boot:run
