#!/bin/bash

set -a
source .env
set +a

./mvnw spring-boot:run \
  -Dspring-boot.run.profiles=dev \
  -Dspring-boot.run.jvmArguments="--enable-native-access=ALL-UNNAMED -Djava.awt.headless=false"