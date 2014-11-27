#!/bin/bash

if [ ! -f "${CONFIG}" ]; then 
	#create default configuration
	cp /target/ais-coverage-0.2-SNAPSHOT-dist/ais-coverage-0.2-SNAPSHOT/coverage-fromtcp-sample.xml $CONFIG
fi

cd /target/ais-coverage*/*/

/bin/bash coverage.sh -file $CONFIG
