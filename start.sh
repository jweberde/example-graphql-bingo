#!/bin/bash
PROJECT_VERSION=$(./gradlew properties -q | grep "version:" | awk '{print $2}')
exec docker run --rm -p 8080:8080 weber.de.example.graphql.bingo/bingo-api:${PROJECT_VERSION}