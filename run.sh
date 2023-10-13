docker run -d \
  --name="nextcloud-dlna" \
  --net=host \
  -e NEXTCLOUD_DLNA_INTERFACE=en0 \
  -e NEXTCLOUD_DB_HOST='192.168.0.88' \
  -e NEXTCLOUD_DB_PASS='p4szt3t!' \
  -v '/Users/xis/projects/playground/docker/nextcloud/app/data:/nextcloud' \
  -e NEXTCLOUD_DATA_DIR=/nextcloud \
  nextcloud-dlna
