FROM openjdk:13-jdk-alpine

ARG BUILD_VERSION
ADD mplatform-${BUILD_VERSION}.jar mplatform.jar

EXPOSE 8888

ENTRYPOINT exec java -jar /mplatform.jar