#!/bin/bash

set -e # exit on error

ARG_LINE=
if [[ "$TEST_TYPE" == "reflection" ]]; then
    ARG_LINE="$ARG_LINE -Djnr.ffi.asm.enabled=false"
fi
for name in x86asm fast-int fast-long fast-numeric; do
    if [[ "$TEST_TYPE" != $name* ]]; then
        ARG_LINE="$ARG_LINE -Djnr.ffi.${name}.enabled=false"
    fi
done
if [[ "$TEST_TYPE" == "all"* ]] || [ ! "$TEST_TYPE" ]; then
    unset ARG_LINE
fi
if [[ "$TEST_TYPE" == *"32" ]]; then
    wget --no-check-certificate -c --header "Cookie: oraclelicense=accept-securebackup-cookie" http://download.oracle.com/otn-pub/java/jdk/8u152-b16/aa0333dd3019491ca4f6ddbe78cdb6d0/jdk-8u152-linux-i586.tar.gz
    tar -xf jdk-8u152-linux-i586.tar.gz
    sudo apt-get -f install lib32z1 libc6-dev-i386
    export JAVA_HOME=`pwd`/jdk1.8.0_152
fi
echo JAVA_HOME: ${JAVA_HOME}
echo Arguments: ${ARG_LINE}
./mvnw -Djacoco.propertyName=jacocoArgLine "-DargLine=@{jacocoArgLine} ${ARG_LINE}" verify -B
