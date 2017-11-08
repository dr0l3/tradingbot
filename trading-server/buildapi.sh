#!/usr/bin/env bash
gradle clean fatjarapi
docker build -t dr0l3/trading-bot-api -f Dockerfile.API .