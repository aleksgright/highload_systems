FROM gradle:jdk21 as build
WORKDIR /config-server

# 1️⃣ Copy Gradle metadata only
COPY build.gradle.kts ./
COPY gradle gradle

# 2️⃣ Warm dependency + plugin cache
RUN --mount=type=cache,id=gradle-cache-gateway,target=/home/gradle/.gradle \
    gradle help  --no-daemon

# 3️⃣ Copy source
COPY src src

# 4️⃣ Build (FAST now)
RUN --mount=type=cache,id=gradle-cache-gateway,target=/home/gradle/.gradle \
    gradle build -x test --no-daemon

FROM amazoncorretto:21
WORKDIR /app
COPY --from=build /config-server/build/libs/*.jar config-server.jar
ENTRYPOINT ["java","-jar","config-server.jar"]