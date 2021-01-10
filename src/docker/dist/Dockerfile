FROM adoptopenjdk:11-jre-hotspot

ARG projectVersion
ARG projectName

LABEL weber.de.scm="https://github.com/jweberde/example-graphql-bingo.git"

VOLUME /tmp

RUN adduser --system --shell /bin/bash --ingroup users bingo-app
WORKDIR /home/bingo-app


COPY bingo-app-${projectVersion}.jar bingo-app.jar
COPY logback-spring-docker.xml logback-spring-docker.xml
COPY logback-spring.xml logback-spring.xml
COPY start.sh start.sh

RUN echo "${projectVersion}" > APP_VERSION && echo "${projectName}" > APP_NAME
RUN chmod +x start.sh && chmod +r APP_VERSION APP_NAME
RUN chmod -R 777 /tmp

RUN sh -c 'touch bingo-app.jar'

USER bingo-app

EXPOSE 8080

CMD ["/home/bingo-app/start.sh"]
