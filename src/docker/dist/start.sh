#!/bin/bash
set -e
TARGET_ENV=${TARGET_ENV:-DEV}

BASEDIR=$(dirname "$0")

# Use Environment SPRING_PROFILE to overwrite additional profiles.

echo "Current Target Environment: ${TARGET_ENV}"
echo "BaseDir: ${BASEDIR}"

# DEFINE GELF_LOG_HOST  e.g. udp:localhost
# DEFINE GELF_LOG_PORT  e.g. 12201

# Log with JSON
export LOGGING_APPENDER="CONSOLE_JSON"
export APP_NAME="eas-api"

SYS_PARAMS="${SYS_PARAMS:-""}"
SYS_PARAMS_EXT=${TARGET_ENV:-""}

export LOG_HOSTNAME="${HOSTNAME}"
export LOG_FACILITY="${TARGET_ENV}|${APP_NAME}"



if [ -z "${IS_RANCHER}" ]; then
    echo "Not using Rancher."
else
    # Try to gater RANCHER_HOST_NODE
    echo "Calling Rancher MetaData Service: http://rancher-metadata/latest/self/host/name"
    TRY_RANCHER_HOST_NODE=$(curl --fail http://rancher-metadata/latest/self/host/name)
    echo "RancherHostNode: '${TRY_RANCHER_HOST_NODE}'"

    if ! [ -z "${TRY_RANCHER_HOST_NODE}" ]; then
      SYS_PARAMS="${SYS_PARAMS} -Dapp.container.host=${TRY_RANCHER_HOST_NODE}"
      export RANCHER_HOST_NODE="${TRY_RANCHER_HOST_NODE}"
      export LOG_HOSTNAME="${HOSTNAME}.${RANCHER_HOST_NODE}"
    fi
fi


SYS_PARAMS="${SYS_PARAMS} -Dsentry.servername=${LOG_HOSTNAME}"
export APP_VERSION=$(cat ${BASEDIR}/APP_VERSION)
export APP_NAME=$(cat ${BASEDIR}/APP_NAME)


if [ "TEST" = "${TARGET_ENV}" ]; then
    SPRING_PROFILE_EXT="${SPRING_PROFILE:-test}"
    SYS_PARAMS="${SYS_PARAMS} -Dsentry.environment=test"
    SYS_PARAMS="${SYS_PARAMS} -Dsentry.release=${APP_VERSION}-next"
    SYS_PARAMS="${SYS_PARAMS} -Dsentry.dsn=NOT_DEFINED"
    SYS_PARAMS="${SYS_PARAMS} -Dlogging.config=file:${BASEDIR}/logback-spring-docker.xml"
elif [ "QS" = "${TARGET_ENV}" ]; then
    SPRING_PROFILE_EXT="${SPRING_PROFILE:-qs}"
    SYS_PARAMS="${SYS_PARAMS} -Dsentry.environment=qs"
    SYS_PARAMS="${SYS_PARAMS} -Dsentry.release=${APP_VERSION}"
    SYS_PARAMS="${SYS_PARAMS} -Dsentry.dsn=NOT_DEFINED"
    # Switch to -docker when sentry is active
    SYS_PARAMS="${SYS_PARAMS} -Dlogging.config=file:${BASEDIR}/logback-spring-docker.xml"
elif [ "LIVE_BETA" = "${TARGET_ENV}" ]; then
    SPRING_PROFILE_EXT="${SPRING_PROFILE:-live,live-beta}"
    SYS_PARAMS="${SYS_PARAMS} -Dsentry.environment=live_beta"
    SYS_PARAMS="${SYS_PARAMS} -Dsentry.release=${APP_VERSION}"
    SYS_PARAMS="${SYS_PARAMS} -Dsentry.dsn=NOT_DEFINED"
    # Switch to -docker when sentry is active
    SYS_PARAMS="${SYS_PARAMS} -Dlogging.config=file:${BASEDIR}/logback-spring-docker.xml"
elif [ "LIVE" = "${TARGET_ENV}" ]; then
    SPRING_PROFILE_EXT="${SPRING_PROFILE:-live}"
    SYS_PARAMS="${SYS_PARAMS} -Dsentry.environment=live"
    SYS_PARAMS="${SYS_PARAMS} -Dsentry.release=${APP_VERSION}"
    SYS_PARAMS="${SYS_PARAMS} -Dsentry.dsn=NOT_DEFINED"
    # Switch to -docker when sentry is active
    SYS_PARAMS="${SYS_PARAMS} -Dlogging.config=file:${BASEDIR}/logback-spring-docker.xml"
else
    # DEV Enviroment
    export LOGGING_APPENDER="CONSOLE"
    SYS_PARAMS="${SYS_PARAMS} -Dlogging.config=file:${BASEDIR}/logback-spring-docker.xml"
    SPRING_PROFILE_EXT=""
fi

FORWARDED_PARAMETER=${@:1:99}

echo "Loading Application with Profiles: ${SPRING_PROFILE_EXT}"

exec java -Djava.security.egd=file:/dev/./urandom -Dspring.profiles.active=${SPRING_PROFILE_EXT} ${SYS_PARAMS} -Djava.io.tmpdir=/tmp -jar ${BASEDIR}/${APP_NAME}.jar ${FORWARDED_PARAMETER}
