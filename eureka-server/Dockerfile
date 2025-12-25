FROM gradle:jdk21 as build
COPY . /eureka-server/src
WORKDIR /eureka-server/src
RUN ./gradlew clean build -x test

FROM amazoncorretto:21 as runner
WORKDIR /app
COPY --from=build /eureka-server/src/build/libs/*.jar user-accounter.jar
ENTRYPOINT ["java","-jar","user-accounter.jar"]