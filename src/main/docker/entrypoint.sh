#!/bin/sh

exec java ${JAVA_OPTS} -Dserver.port=8080 -Djava.security.egd=file:/dev/./urandom -jar "${HOME}/app.jar" "$@"
