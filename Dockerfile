#FROM gradle:jdk21 as build
#COPY . /src
#WORKDIR /src
#RUN ./gradlew clean build -x test
#
#FROM amazoncorretto:21 as runner
#WORKDIR /app
#COPY --from=build /src/build/libs/*.jar app.jar
#ENTRYPOINT ["java","-jar","app.jar"]