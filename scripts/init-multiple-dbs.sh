#!/bin/bash
# Cria os 3 bancos (um por microsservico) na primeira subida do container Postgres.
set -e

for DB in boletosafe_auth boletosafe_risk boletosafe_scanner; do
  psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-EOSQL
    SELECT 'CREATE DATABASE $DB' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = '$DB')\gexec
EOSQL
done
