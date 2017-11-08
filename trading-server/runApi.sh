#!/usr/bin/env bash
docker run -d -p 8082:4567 --link mongo:mongo --name trading-api dr0l3/trading-bot-api
curl localhost:8082/dashbard