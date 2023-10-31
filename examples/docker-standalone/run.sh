#!/bin/bash

docker run -d \
  --name="nextcloud-dlna" \
  --restart=unless-stopped \
  --net=host \
  -e NEXTCLOUD_DLNA_SERVER_PORT=9999 \
  -e NEXTCLOUD_DLNA_FRIENDLY_NAME="Nextcloud" \
  -e NEXTCLOUD_DB_HOST='localhost' \
  -e NEXTCLOUD_DB_PASS='secret' \
  -v '/opt/nextcloud/data:/nextcloud' \
  -e NEXTCLOUD_DATA_DIR=/nextcloud \
thanek/nextcloud-dlna
