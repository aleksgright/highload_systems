FROM gradle:jdk21 as build
COPY . /main/src
WORKDIR /main/src
RUN ./gradlew build -x test

FROM amazoncorretto:21 as runner
WORKDIR /app
COPY --from=build /main/src/build/libs/*.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]