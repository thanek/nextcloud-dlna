#!/bin/bash

dir=$(realpath "$(dirname "$0")")

cd "$dir/../" || exit

version=$(./gradlew currentVersion | grep "Project version" | awk -F"version: " '{ print $2 }')

echo "This will create docker image for version $version."
read -p "Continue [y/n]? " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    exit
fi

./gradlew clean build || exit

docker buildx create \
  --use \
  --platform=linux/arm/v7,linux/arm64/v8,linux/amd64 \
  --name multi-platform-builder

docker buildx inspect --bootstrap

docker buildx build \
  --push \
  --platform=linux/arm/v7,linux/arm64/v8,linux/amd64 \
  --file=./docker/Dockerfile \
  --tag=thanek/nextcloud-dlna:$version .

docker buildx build \
  --push \
  --platform=linux/arm/v7,linux/arm64/v8,linux/amd64 \
  --file=./docker/Dockerfile \
  --tag=thanek/nextcloud-dlna .

cd - || exit
