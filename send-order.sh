#!/bin/bash

url=http://localhost:8080

json="{\"identifier\":\"123\", \"value\":199}"
curl -d "${json}" -H "Content-Type: application/json" -X POST ${url}/orders
