# Etapa de build usando Maven oficial com Java 17
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build
WORKDIR /app

# Copia os arquivos necessários para o Maven funcionar em modo wrapper
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# Baixa dependências sem copiar o código-fonte (melhor caching)
RUN ./mvnw dependency:go-offline

# Copia o restante do código-fonte
COPY src ./src

# Compila o projeto e gera o .jar final, sem rodar os testes
RUN ./mvnw clean package -DskipTests

# Etapa de execução usando OpenJDK 17 oficial
FROM openjdk:17-alpine
WORKDIR /app

# Copia o .jar gerado da etapa de build
COPY --from=build /app/target/*.jar app.jar

# Expõe a porta padrão do Spring Boot (ajuste se necessário)
EXPOSE 8084

# Comando de entrada
ENTRYPOINT ["java", "-jar", "app.jar"]
