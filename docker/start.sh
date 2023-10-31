#!/bin/bash

# Wait for Nextcloud to come online
if [ -n "$NC_DOMAIN" ] && [ -n "$NC_PORT" ]; then
    while ! nc -z "$NC_DOMAIN" "$NC_PORT"; do
        echo "Waiting for Nextcloud to start on $NC_DOMAIN:$NC_PORT..."
        sleep 5
    done
    echo "Nextcloud found on $NC_DOMAIN:$NC_PORT!"
fi

# Execute CMD
exec /__cacert_entrypoint.sh "$@"
