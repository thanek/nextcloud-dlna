docker buildx create \
  --use \
  --platform=linux/arm/v7,linux/arm64/v8,linux/amd64 \
  --name multi-platform-builder

docker buildx inspect --bootstrap

docker buildx build \
  --push \
  --platform=linux/arm/v7,linux/arm64/v8,linux/amd64 \
  --tag=thanek/nextcloud-dlna .
