#!/usr/bin/env bash
curl localhost:8082/dashboard | jq
curl localhost:8082/allUsers | jq