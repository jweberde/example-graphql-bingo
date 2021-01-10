#!/bin/bash
PROJECT_VERSION=$(cat package.json | jq --raw-output .version)
exec docker run --rm -p 4200:80 weber.de.example.graphql.bingo/bingo-ui:${PROJECT_VERSION}
