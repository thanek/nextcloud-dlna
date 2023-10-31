# Nextcloud DLNA server

[![Java CI with Gradle](https://github.com/thanek/nextcloud-dlna/actions/workflows/gradle.yml/badge.svg)](https://github.com/thanek/nextcloud-dlna/actions/workflows/gradle.yml)

DLNA addon for your self-hosted Nextcloud app instance that allows you to stream Nextcloud users content on client
devices in your network.
It supports the group folders as well.

## Running in Docker

You can use the docker image with nextcloud-dlna e.g.:

```bash
docker run -d \
 --name="nextcloud-dlna" \
 --net=host \
 -v /path/to/nextcloud/app/ending/with/data:/nextcloud \
 -e NEXTCLOUD_DATA_DIR=/nextcloud \
 -e NEXTCLOUD_DB_HOST='<your_nextcloud_db_host_ip_here>' \
 -e NEXTCLOUD_DB_PASS='<your_nextcloud_db_pass_here>' \
thanek/nextcloud-dlna
```

or, if used together with the official Nextcloud docker image using the docker-composer. See the [examples](./examples)
directory. for more details about running nextcloud-dlna server in the docker container.

While using docker, you can pass to the container the `NC_DOMAIN` and `NC_PORT` environment variables and force the container 
to check and wait for the nextcloud HTTP server to appear on the specified domain and port address.
You can also pass to the container other env variables that are listed below.

Note that it would not work on Mac OS since docker is a Linux container and the `host` networking mode doesn't actually
share the host's network interfaces.

See https://hub.docker.com/r/thanek/nextcloud-dlna for more docker image details.

## Building the project

Build the project with:

`./gradlew clean bootRun`

You can also pass the environment variables without editing the config files:

`NEXTCLOUD_DLNA_SERVER_PORT=9999 ./gradlew clean bootRun`

or, if you've already built the project and created the jar file:

`NEXTCLOUD_DLNA_SERVER_PORT=9999 java -jar nextcloud-dlna-X.Y.Z.jar`

## ENV variables

Available env variables with their default values that you can overwrite:

| env variable                 | default value  | description                                                                                                   |  
|------------------------------|----------------|---------------------------------------------------------------------------------------------------------------|
| NEXTCLOUD_DLNA_SERVER_PORT   | 8080           | port on which the contentController will listen                                                               |
| NEXTCLOUD_DLNA_INTERFACE     |                | (optional) interface the server will be listening on<br/>if not given, the default local address will be used |
| NEXTCLOUD_DLNA_FRIENDLY_NAME | Nextcloud-DLNA | friendly name of the DLNA service                                                                             |
| NEXTCLOUD_DATA_DIR           |                | nextcloud installation directory (that ends with /data)                                                       |
| NEXTCLOUD_DB_TYPE            | mariadb        | nextcloud database type (mysql, mariadb, postgres)                                                            |
| NEXTCLOUD_DB_HOST            | localhost      | nextcloud database host                                                                                       |
| NEXTCLOUD_DB_PORT            | 3306           | nextcloud database port                                                                                       |
| NEXTCLOUD_DB_NAME            | nextcloud      | nextcloud database name                                                                                       |
| NEXTCLOUD_DB_USER            | nextcloud      | nextcloud database username                                                                                   |
| NEXTCLOUD_DB_PASS            | nextcloud      | nextcloud database password                                                                                   |

### Code used

Some java code was taken from https://github.com/haku/dlnatoad
and https://github.com/UniversalMediaServer/UniversalMediaServer converted to Kotlin with upgrade to jupnp instead of
cling.
