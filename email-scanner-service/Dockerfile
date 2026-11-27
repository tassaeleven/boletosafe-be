FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Certificado corporativo opcional: só existe na máquina local (rede com inspeção TLS).
# O "[t]" faz o COPY não falhar quando o arquivo não existe (ex.: build no Render).
COPY pom.xml empresa-ca.cr[t] /tmp/
RUN if [ -f /tmp/empresa-ca.crt ]; then \
      keytool -importcert -noprompt -trustcacerts -cacerts \
        -storepass changeit -alias empresa-ca -file /tmp/empresa-ca.crt; \
    fi

COPY pom.xml .
RUN mvn -q dependency:go-offline
COPY src ./src
RUN mvn -q clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
