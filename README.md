# Nextcloud DLNA server

DLNA addon for your self-hosted Nextcloud app instance that allows you to stream Nextcloud users content on client
devices in your network.
It supports the group folders as well.

Just edit the `application.yml` and rebuild the project with:

`./gradlew clean bootRun`

###  

Some java code was taken from https://github.com/haku/dlnatoad
and https://github.com/UniversalMediaServer/UniversalMediaServer converted to Kotlin with upgrade to jupnp instead of
cling.