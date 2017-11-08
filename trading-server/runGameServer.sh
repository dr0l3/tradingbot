#!/usr/bin/env bash
docker rm -f trading-game-server
docker run -d --link mongo:mongo --name trading-game-server dr0l3/trading-bot-game-server