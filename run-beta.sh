#!/bin/bash
cd "$(dirname "$0")"
echo "Aleatorium beta"
sleep 10
qjackctl &
sleep 10

sudo java -cp beta/aleatorium-beta.jar:beta/lib/rpi-ws281x-java-2.0.0-SNAPSHOT.jar de.sciss.aleatorium.Light &

sleep 4

java -cp beta/aleatorium-beta.jar de.sciss.aleatorium.Beta
