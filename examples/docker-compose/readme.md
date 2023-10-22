This will run the nextcloud-dlna in docker together with the full Nextcloud installation (containing the app, database 
and redis) located in the `./app` directory.

Note: in order to enable network access to the MariaDB server, after the first run, you'll need to edit 
the `./etc/mariadb.cnf`, section `[client-config]` by adding the line:
```
port = 3306
```
and removing the line:
```
socket = /var/run/mysqld/mysqld.sock
```
, then restart the `db` (`nextcloud-db-1`) container.