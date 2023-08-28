#!/usr/bin/env bash
# See document INTERNAL-PUBLISH-JAR.md for publishing instructions

./mvnw clean install deploy -pl client,api
