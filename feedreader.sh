#!/usr/bin/env bash

if [ ! -f "./demo-app-feedreader/target/demo-app-feedreader-1.0-SNAPSHOT-jar-with-dependencies.jar" ]; then
    ./mvnw clean package
fi

java -jar ./demo-app-feedreader/target/demo-app-feedreader-1.0-SNAPSHOT-jar-with-dependencies.jar $1 $2 $3 $4 $5 $6 $7 $8 $9