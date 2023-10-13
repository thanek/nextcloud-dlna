# Nextcloud DLNA server

DLNA addon for your self-hosted Nextcloud app instance that allows you to stream Nextcloud users content on client
devices in your network.
It supports the group folders as well.

Just edit the `application.yml` and rebuild the project with:

`./gradlew clean bootRun`

You can also pass the environment variables without editing the config files:

`NEXTCLOUD_DLNA_SERVER_PORT=9999 ./gradlew clean bootRun`

or, if you've already built the project and created the jar file:

`NEXTCLOUD_DLNA_SERVER_PORT=9999 java -jar nextcloud-dlna-X.Y.Z.jar`

Available env variables with their default values that you can overwrite:

| env variable                 | default value  | description                                             |  
|------------------------------|----------------|---------------------------------------------------------|
| NEXTCLOUD_DLNA_SERVER_PORT   | 8080           | port on which the contentController will listen         |
| NEXTCLOUD_DLNA_INTERFACE     | eth0           | interface the server will be listening on               |
| NEXTCLOUD_DLNA_FRIENDLY_NAME | Nextcloud-DLNA | friendly name of the DLNA service                       |
| NEXTCLOUD_DATA_DIR           |                | nextcloud installation directory (that ends with /data) |
| NEXTCLOUD_DB_HOST            | localhost      | nextcloud database host                                 |
| NEXTCLOUD_DB_PORT            | 3306           | nextcloud database port                                 |
| NEXTCLOUD_DB_NAME            | nextcloud      | nextcloud database name                                 |
| NEXTCLOUD_DB_USER            | nextcloud      | nextcloud database username                             |
| NEXTCLOUD_DB_PASS            | nextcloud      | nextcloud database password                             |


## Running in Docker
You can build your Docker image using the `docker build -t nextcloud-dlna .` command. Next, you need to run the docker 
image with the `net=host` option e.g:

```
docker run -d \
--name="nextcloud-dlna" \
--net=host \
-v /path/to/nextcloud/app/ending/with/data:/nextcloud \
-e NEXTCLOUD_DATA_DIR=/nextcloud \
-e NEXTCLOUD_DB_HOST='<your_host_ip_here>' \
-e NEXTCLOUD_DB_PASS='<your_nextcloud_db_pass_here>' \
nextcloud-dlna
```

You can pass to the container other env variables that are listed above.

Note that it would not work on Mac OS since docker is a Linux container and the `host` networking mode doesn't actually 
share the host's network interfaces.

### Code used 

Some java code was taken from https://github.com/haku/dlnatoad
and https://github.com/UniversalMediaServer/UniversalMediaServer converted to Kotlin with upgrade to jupnp instead of
cling.