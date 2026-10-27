# ---------- Etapa 1: compilar ----------
FROM gradle:9.5.0-jdk21 AS build
WORKDIR /src
COPY . .
# installDist deja el servidor con todas sus librerías en build/install/tratohecho-servidor
RUN gradle installDist --no-daemon --console=plain -q

# ---------- Etapa 2: ejecutar ----------
FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S app && adduser -S app -G app
WORKDIR /app
COPY --from=build /src/build/install/tratohecho-servidor/ ./
USER app

# La JVM usa como máximo el 75% de la memoria del contenedor (mem_limit en compose.yml)
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75"
ENV PUERTO=8080
EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
  CMD wget -qO- http://localhost:8080/salud || exit 1

CMD ["bin/tratohecho-servidor"]
