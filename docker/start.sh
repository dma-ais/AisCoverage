#!/bin/bash

cd /ais-coverage

sed -i "s/PLACEHOLDER/${SERVER}:${PORT}/g" CONFIG_BASE.xml
sed -i "s/<source>.*<\/source>/<source>${SOURCE_NAME}<\/source>/g" CONFIG_BASE.xml



CONFIG="CONFIG_BASE.xml"
if [ "${CONFIGURL}" ]; then 
	curl -sS $CONFIGURL > CONFIG.xml
	CONFIG="CONFIG.xml"
fi

pwd
ls .
/bin/bash coverage.sh -file $CONFIG
