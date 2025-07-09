FROM amazoncorretto:21
EXPOSE 9000
COPY ./build/libs/*.jar ./app.jar

ENV SPRING_PROFILES_ACTIVE=prod
ENTRYPOINT ["java", "-jar", "app.jar"]
