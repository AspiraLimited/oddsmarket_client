#!/usr/bin/env bash

if [ ! -f "./demo-app-tradingfeedreader/target/demo-app-tradingfeedreader-1.0-SNAPSHOT-jar-with-dependencies.jar" ]; then
    ./mvnw clean package -s demo-app-tradingfeedreader/settings-public.xml
fi

java -jar ./demo-app-tradingfeedreader/target/demo-app-tradingfeedreader-1.0-SNAPSHOT-jar-with-dependencies.jar $1 $2 $3 $4 $5 $6 $7 $8 $9