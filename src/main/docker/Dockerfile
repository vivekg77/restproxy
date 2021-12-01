FROM metrodigital.azurecr.io/mb-alpine-openjdk-base:jre-11-latest

ENV JAVA_OPTS=""

RUN apk update && apk add --no-cache libc6-compat gcompat
ADD entrypoint.sh entrypoint.sh
RUN chmod 755 entrypoint.sh && chown springboot:springboot entrypoint.sh
USER springboot

ENTRYPOINT ["./entrypoint.sh"]

EXPOSE 8080

ADD *.jar app.jar
