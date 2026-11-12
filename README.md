# Nextcloud DLNA server

[![Java CI with Gradle](https://github.com/thanek/nextcloud-dlna/actions/workflows/gradle.yml/badge.svg)](https://github.com/thanek/nextcloud-dlna/actions/workflows/gradle.yml)

DLNA addon for your self-hosted Nextcloud app instance that allows you to stream Nextcloud users content on client
devices in your network.
It supports the group folders as well.
Supports client-requested sorting of browse results by title, date, and media type (`dc:title`, `dc:date`, `upnp:class`).
Clients that don't request any sorting get the results sorted by title (configurable, see [Default sort order](#default-sort-order)).

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
| NEXTCLOUD_DLNA_DEFAULT_SORT  | +dc:title      | sort order used for clients that don't request one (see Default sort order)                                   |
| NEXTCLOUD_DATA_DIR           |                | nextcloud installation directory (that ends with /data)                                                       |
| NEXTCLOUD_SCANNED_FOLDERS    | /**            | folders to be exposed by the DLNA service (use Glob Pattern to enumerate your folders)                        |
| NEXTCLOUD_DB_TYPE            | mariadb        | nextcloud database type (mysql, mariadb, postgres)                                                            |
| NEXTCLOUD_DB_HOST            | localhost      | nextcloud database host                                                                                       |
| NEXTCLOUD_DB_PORT            | 3306           | nextcloud database port                                                                                       |
| NEXTCLOUD_DB_NAME            | nextcloud      | nextcloud database name                                                                                       |
| NEXTCLOUD_DB_USER            | nextcloud      | nextcloud database username                                                                                   |
| NEXTCLOUD_DB_PASS            | nextcloud      | nextcloud database password                                                                                   |

### Default sort order
Many client devices (TVs in particular) browse without asking the server for any particular order. Those clients get the
results sorted by `NEXTCLOUD_DLNA_DEFAULT_SORT`, which is a comma separated list of UPnP sort criteria, each of them
prefixed with `+` (ascending) or `-` (descending):

| criterion    | meaning                                                       |
|--------------|---------------------------------------------------------------|
| `dc:title`   | file/folder name (case insensitive)                           |
| `dc:date`    | modification time                                             |
| `upnp:class` | media type (audio, then image, then video)                    |

Examples:
`NEXTCLOUD_DLNA_DEFAULT_SORT="-dc:date"` - newest files first
`NEXTCLOUD_DLNA_DEFAULT_SORT="+upnp:class,+dc:title"` - group by media type, then by name
`NEXTCLOUD_DLNA_DEFAULT_SORT=""` - no default sorting (the order comes from the Nextcloud database and may change over time)

Sort criteria requested by the client always take precedence over the default ones. Folders are sorted by name whenever
none of the criteria applies to them (they have no date nor media type).

### Scanned Folders
By default nextcloud-dlna scans all the folders that are available to serve, i.e. all users' folders and all global 
folders (if the Group folders feature is enabled in the Nextcloud config). You may filter the folders to be served by 
passing the env variable with the list of allowed folders using the Glob Pattern. In this pattern you can use the 
placeholders (precesed by a slash):

| Placeholder | Meaning                                                       |
|-------------|---------------------------------------------------------------|
| `*`         | any string in the path segment                                |
| `**`        | any one or more folders recursively (requires at least one segment) |
| `?`         | any character                                                 |
| `{foo,bar}` | alternatives                                                  |
| `[abc]`     | one of the characters from the set                            |

Examples:
`NEXTCLOUD_SCANNED_FOLDERS="/john,/jane,/jim,/joe"` - will allow to scan only the four users' folders
`NEXTCLOUD_SCANNED_FOLDERS="/**/music"` - will allow to scan only the `music` folder of any user (e.g. `/john/music` or `/john/rock/music`, but not a top-level `/music`)
`NEXTCLOUD_SCANNED_FOLDERS="/{jim,joe}/**/music"` - will allow to scan only the `music` folder of `jim` and `joe` users
`NEXTCLOUD_SCANNED_FOLDERS="/**/{music,photos,movies}"` - will allow to scan only the `music`,`photos` and `movies` folders of any user

### Code used

Some java code was taken from https://github.com/haku/dlnatoad
and https://github.com/UniversalMediaServer/UniversalMediaServer converted to Kotlin with upgrade to jupnp instead of
cling.
