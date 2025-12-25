FROM gradle:jdk21 as build
COPY . /config-server/src
WORKDIR /config-server/src
RUN ./gradlew clean build -x test

FROM amazoncorretto:21 as runner
WORKDIR /app
COPY --from=build /config-server/src/build/libs/*.jar user-accounter.jar
ENTRYPOINT ["java","-jar","user-accounter.jar"]