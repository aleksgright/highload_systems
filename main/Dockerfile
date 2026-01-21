FROM gradle:jdk21 as build
WORKDIR /main

# 1️⃣ Copy Gradle metadata first (stable = cached)
COPY build.gradle.kts ./
COPY gradle gradle

# 2️⃣ Warm Gradle cache properly
RUN --mount=type=cache,id=gradle-cache-app,target=/home/gradle/.gradle \
    gradle help  --no-daemon

# 3️⃣ Copy source code last
COPY src src

# 4️⃣ Build (fast on rebuilds)
RUN --mount=type=cache,id=gradle-cache-app,target=/home/gradle/.gradle \
    gradle build -x test --no-daemon

FROM amazoncorretto:21
WORKDIR /app
COPY --from=build /main/build/libs/*.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]