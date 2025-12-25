FROM gradle:jdk21 as build
COPY . /gateway/src
WORKDIR /gateway/src
RUN ./gradlew build -x test

FROM amazoncorretto:21 as runner
WORKDIR /app
COPY --from=build /gateway/src/build/libs/*.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]