#!/usr/bin/env bash

scheduleConnector=http://localhost:8080/schedules

for i in {1..1000}
do
  customer="Customer${i}"
  documentNumber="0000${i}"
  description="Test ${i}"
  dateNow=$(date +"%FT%T")

  json="{\"dateTime\":\"${dateNow}\",\"customer\":{\"name\":\"${customer}\",\"documentNumber\":\"${documentNumber}\",\"phone\":\"4499999999\"},\"description\":\"${description}\"}"

  response=$(curl -s -d "${json}" -H "Content-Type: application/json" -X POST ${scheduleConnector})
  echo ${response}

done