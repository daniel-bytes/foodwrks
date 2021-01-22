#!/bin/bash

if [[ $(docker ps -a | grep foodwrks-postgres) ]]; then
  echo "Removing existing container"
  docker rm -vf foodwrks-postgres
fi

echo "Creating container"
docker run -p 5432:5432 --name foodwrks-postgres -e POSTGRES_PASSWORD=password -d postgres:13.1-alpine

echo "Waiting for Pg to start up"
sleep 5

echo "Creating database"
echo "CREATE DATABASE foodwrks" | docker exec -i foodwrks-postgres psql -U postgres

echo "Test database created"