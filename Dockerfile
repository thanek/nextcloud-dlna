FROM eclipse-temurin:17-jdk
WORKDIR /app

COPY ./build/libs/* ./nextcloud-dlna.jar

EXPOSE 8080
CMD ["java","-jar","nextcloud-dlna.jar"]
