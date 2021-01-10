#!/bin/sh
set -e
echo "Starting with Environment: ${APP_ENVIRONMENT} ${APP_VERSION} ${APP_LANGS} ${APP_REVISION}"
sed -e "s/###APP_ENVIRONMENT###/${APP_ENVIRONMENT}/" /etc/nginx/conf.d/angular-spa-ui.template \
| sed -e "s/###APP_REVISION###/${APP_REVISION}/" \
| sed -e "s/###APP_VERSION###/${APP_VERSION}/" > /etc/nginx/conf.d/default.conf \
&& exec nginx -g 'daemon off;'
