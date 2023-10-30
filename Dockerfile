FROM eclipse-temurin:17-jdk
WORKDIR /app

COPY ./build/libs/* ./nextcloud-dlna.jar

RUN set -ex; \
    export DEBIAN_FRONTEND=noninteractive; \
    apt-get update; \
    apt-get install -y --no-install-recommends \
        tzdata \
        netcat-openbsd \
    ; \
    rm -rf /var/lib/apt/lists/*;

COPY --chmod=775 start.sh /start.sh

EXPOSE 8080
ENTRYPOINT /start.sh
CMD ["java","-jar","nextcloud-dlna.jar"]
