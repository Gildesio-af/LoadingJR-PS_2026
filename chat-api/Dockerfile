# --- Estágio 1: Build (Compilação) ---
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app

# Copia o pom.xml para baixar dependências primeiro (Cache Layer)
COPY pom.xml .

# Baixa as dependências e plugins (Isso agiliza builds futuros)
RUN mvn dependency:go-offline

# Copia o código fonte
COPY src ./src

# Compila o projeto e gera o JAR (Pula testes para agilizar o build no Docker)
RUN mvn clean package -DskipTests

# --- Estágio 2: Runtime (Execução) ---
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Cria usuário restrito por segurança
RUN addgroup --system appgroup && adduser --system --ingroup appgroup appuser

# Copia o JAR gerado no estágio anterior
# O comando *.jar pega qualquer nome que o maven tenha gerado
COPY --from=builder /app/target/*.jar app.jar

# Define permissões
RUN chown appuser:appgroup app.jar

# Define usuário seguro
USER appuser

# Expõe a porta padrão do Spring Boot
EXPOSE 8080

# Configurações de Memória da JVM para Containers
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]