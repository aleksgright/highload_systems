FROM gradle:jdk21 as build
COPY . /user-accounter/src
WORKDIR /user-accounter/src
RUN ./gradlew build -x test

FROM amazoncorretto:21 as runner
WORKDIR /app
COPY --from=build /user-accounter/src/build/libs/*.jar user-accounter.jar
ENTRYPOINT ["java","-jar","user-accounter.jar"]