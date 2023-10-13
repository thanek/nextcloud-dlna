FROM eclipse-temurin:17-jdk-focal
WORKDIR /app

COPY ./build/libs/* ./app.jar

EXPOSE 8080
CMD ["java","-jar","app.jar"]
