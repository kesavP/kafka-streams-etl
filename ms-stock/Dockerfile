FROM eclipse-temurin:26-jre-alpine
WORKDIR /opt
EXPOSE 9093
COPY target/*.jar /opt/app.jar
ENTRYPOINT exec java $JAVA_OPTS -jar app.jar