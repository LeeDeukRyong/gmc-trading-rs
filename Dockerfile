FROM eclipse-temurin:19.0.1_10-jre-alpine
VOLUME /tmp
COPY build/libs/app.jar app.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-Duser.country=KR","-Duser.language=ko","-Duser.timezone=UTC","-jar","/app.jar"]