# Builds and runs destiny-app (the Spring Boot assembly module). This is a
# multi-module Maven reactor (see root pom.xml) — destiny-app depends on every
# sibling module, so the build stage needs the whole reactor in its context,
# not just destiny-app/. .dockerignore excludes destiny-web (a separate npm
# project, deployed elsewhere) and build/VCS clutter to keep the context small.

# --- Build stage ---
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY . .

# Tests already run in CI (.github/workflows/build.yml) against both H2 and a
# real Postgres service container; skipping them here keeps the image build
# fast and independent of any database being reachable at build time.
RUN mvn -B -pl destiny-app -am package -DskipTests

# --- Runtime stage ---
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

COPY --from=build /workspace/destiny-app/target/destiny-app-*.jar app.jar

# Render injects PORT at runtime; application.yml binds server.port to it
# (falling back to 8080 for local/Docker-only runs where PORT is unset).
EXPOSE 8080

# JAVA_OPTS is empty by default; Render lets you set it as an env var, e.g.
# -XX:MaxRAMPercentage=75.0 to keep the JVM heap inside the instance's memory
# limit on smaller plans.
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
