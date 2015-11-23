#!/usr/bin/env bash

mvn clean assembly:assembly
java -jar target/Challenge-*.jar RAW_CONFERENCE_EVENTS.txt