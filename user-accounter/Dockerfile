FROM gradle:jdk21 as build
COPY . /src
WORKDIR /src
RUN gradle clean bootJar

FROM amazoncorretto:21 as runner
WORKDIR /app
COPY --from=build /src/src/build/libs/*.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]