# see https://hub.docker.com/_/nginx/
FROM nginx:alpine

ARG projectVersion
ARG projectName

LABEL weber.de.scm="https://github.com/jweberde/example-graphql-bingo.git"

ENV APP_ENVIRONMENT live
ENV APP_VERSION ${projectVersion}
ENV APP_REVISION ${revision}

COPY docker/start.sh start.sh
RUN chmod +x /start.sh

COPY docker/angular-spa-ui.template /etc/nginx/conf.d/angular-spa-ui.template
COPY dist/${projectName} /usr/share/nginx/html_live

CMD [ "/start.sh" ]
