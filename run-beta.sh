#!/bin/bash
cd "$(dirname "$0")"
echo "Aleatorium alpha"
sleep 10
qjackctl &
sleep 10

java -cp alpha/aleatorium-beta.jar de.sciss.aleatorium.Beta
