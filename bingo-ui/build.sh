#!/bin/bash
set -e
rm -rf ./dist
npm i
ng build --prod
PROJECT_VERSION=$(cat package.json | jq --raw-output .version)
docker build -f docker/Dockerfile -t weber.de.example.graphql.bingo/bingo-ui:${PROJECT_VERSION} . --build-arg projectVersion=${PROJECT_VERSION} --build-arg projectName=bingo-ui
