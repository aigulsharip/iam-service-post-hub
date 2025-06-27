#!/usr/bin/env bash

PROFILE=${PROFILE:-local-idea}
JAR_NAME=iam-service-0.0.1-SNAPSHOT.jar

echo "Starting service with profile: $PROFILE"
exec java -jar /srv/$JAR_NAME --spring.profiles.active=$PROFILE
