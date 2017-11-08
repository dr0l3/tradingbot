#!/usr/bin/env bash
gradle clean fatjarGameServer
docker build -t dr0l3/trading-bot-game-server -f Dockerfile.GameServer .