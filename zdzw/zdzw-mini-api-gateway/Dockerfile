##Build UI
FROM node:20-alpine
ARG BUILD_FLAGS=--configuration=production
WORKDIR app
ENV PATH=${PATH}:/app/node_modules/.bin

# Install dependencies
COPY ui_module/package*.json ./
RUN npm ci

# Compile project
COPY ui_module/ ./
RUN ng build ${BUILD_FLAGS}

##Build main
FROM maven:3.9.1-eclipse-temurin-17

COPY ./pom.xml pom.xml
RUN mvn -s /usr/share/maven/ref/settings.xml dependency:go-offline

COPY ./src src

RUN mvn package

##Run
FROM eclipse-temurin:17

# Copy UI build
COPY --from=0 app/dist/mini-api-gateway-ui/browser/ /public/ui

COPY --from=1 target/*.jar run.jar

EXPOSE 8080
CMD ["java", "-jar", "run.jar"]
