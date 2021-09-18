#!/bin/bash
cd "$(dirname "$0")"

scp -r pi@192.168.0.46:Documents/projects/Aleatorium/workspaces/AleatoriumBeta.mllt workspaces/
