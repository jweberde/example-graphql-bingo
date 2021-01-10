#!/bin/bash
set -e
./gradlew clean build installDockerDist

PROJECT_VERSION=$(./gradlew properties -q | grep "version:" | awk '{print $2}')

docker build -t weber.de.example.graphql.bingo/bingo-api:${PROJECT_VERSION} ./build/install/bingo-app-docker \
--build-arg projectVersion=${PROJECT_VERSION} \
--build-arg projectName=bingo-app