FROM gradle:jdk21 as build
WORKDIR /user-accounter

# 1️⃣ Copy only Gradle metadata first (stable = cached)
COPY build.gradle.kts ./
COPY gradle gradle

# 2️⃣ Warm Gradle cache (REAL cache)
RUN --mount=type=cache,id=gradle-cache-user-accounter,target=/home/gradle/.gradle \
    gradle help --no-daemon

# 3️⃣ Copy source code last
COPY src src

# 4️⃣ Build (FAST)
RUN --mount=type=cache,id=gradle-cache-user-accounter,target=/home/gradle/.gradle \
    gradle build -x test --no-daemon

FROM amazoncorretto:21
WORKDIR /app
COPY --from=build /user-accounter/build/libs/*.jar user-accounter.jar
ENTRYPOINT ["java","-jar","user-accounter.jar"]