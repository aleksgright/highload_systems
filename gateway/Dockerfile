FROM gradle:jdk21 as build
WORKDIR /gateway

# 1️⃣ Copy Gradle metadata only (stable = cached)
COPY build.gradle.kts ./
COPY gradle gradle

# 2️⃣ Warm Gradle cache safely (no source needed)
RUN --mount=type=cache,id=gradle-cache-gateway,target=/home/gradle/.gradle \
    gradle help --no-daemon

# 3️⃣ Copy source code
COPY src src

# 4️⃣ Build
RUN --mount=type=cache,id=gradle-cache-gateway,target=/home/gradle/.gradle \
    gradle build -x test --no-daemon

FROM amazoncorretto:21
WORKDIR /app
COPY --from=build /gateway/build/libs/*.jar gateway.jar
ENTRYPOINT ["java","-jar","gateway.jar"]