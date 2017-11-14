#!/usr/bin/env bash
rm -rf dist
elm make src/Main.elm --output=./dist/main.js
cp index.html ./dist/index.html
docker build -t dr0l3/trading-frontend .
docker push dr0l3/trading-frontend
