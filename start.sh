#!/bin/bash

# Wait for Nextcloud to come online
if [ -n "$NC_DOMAIN" ] && [ "$NC_PORT" ]; then
    while ! nc -z "$NC_DOMAIN" "$NC_PORT"; do
        echo "Waiting for Nextcloud to start..."
        sleep 5
    done
fi

# Execute CMD
exec /__cacert_entrypoint.sh "$@"
