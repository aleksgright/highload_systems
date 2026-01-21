FROM gradle:jdk21 as build
WORKDIR /eureka-server

# 1️⃣ Copy Gradle metadata only
COPY build.gradle.kts ./
COPY gradle gradle

# 2️⃣ Warm dependency + plugin cache
RUN --mount=type=cache,id=gradle-cache-eureka,target=/home/gradle/.gradle \
    gradle help --no-daemon

# 3️⃣ Copy source code
COPY src src

# 4️⃣ Build (FAST)
RUN --mount=type=cache,id=gradle-cache-eureka,target=/home/gradle/.gradle \
    gradle build -x test --no-daemon

FROM amazoncorretto:21
WORKDIR /app
COPY --from=build /eureka-server/build/libs/*.jar eureka-server.jar
ENTRYPOINT ["java","-jar","eureka-server.jar"]