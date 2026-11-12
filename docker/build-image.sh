#!/bin/bash
set -euo pipefail

builder=multi-platform-builder
platforms=linux/arm/v7,linux/arm64/v8,linux/amd64
image=thanek/nextcloud-dlna

dir=$(realpath "$(dirname "$0")")

cd "$dir/../"

version=$(./gradlew currentVersion | grep "Project version" | awk -F"version: " '{ print $2 }')

echo "This will create docker image for version $version."
read -p "Continue [y/n]? " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    exit
fi

./gradlew clean build

docker buildx inspect "$builder" >/dev/null 2>&1 \
  || docker buildx create --name "$builder" --platform="$platforms"

docker buildx inspect --builder="$builder" --bootstrap

docker buildx build \
  --builder="$builder" \
  --push \
  --platform="$platforms" \
  --file=./docker/Dockerfile \
  --tag="$image:$version" \
  --tag="$image" .
