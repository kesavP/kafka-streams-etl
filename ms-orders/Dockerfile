FROM eclipse-temurin:26-jre-alpine
WORKDIR /opt
EXPOSE 9091
COPY target/*.jar /opt/app.jar
ENV SPRING_PROFILES_ACTIVE=DEFAULT
ENTRYPOINT ["java", "-Dspring.profiles.active=intg", "-jar", "app.jar"]