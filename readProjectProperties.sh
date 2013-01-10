#!/bin/bash

for PROP in $(cat "`dirname $0`/project.properties"); do 
	PROP_KEY=$(echo $PROP | cut -d'=' -f1)
	PROP_VALUE=$(echo $PROP | cut -d'=' -f2)
	eval export $PROP_KEY="$PROP_VALUE"
done
