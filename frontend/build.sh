#!/usr/bin/env bash
elm make src/Main.elm --output=./dist/main.js
cp index.html ./dist/index.html
docker build -t dr0l3/trading-frontend .
