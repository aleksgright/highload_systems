FROM amazoncorretto:21
WORKDIR /app
COPY build/libs/*.jar menu-service.jar
ENTRYPOINT ["java","-jar","menu-service.jar"]