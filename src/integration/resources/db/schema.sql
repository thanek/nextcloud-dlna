DROP TABLE IF EXISTS oc_appconfig;
CREATE TABLE `oc_appconfig` (
  `appid` varchar(32) NOT NULL DEFAULT '',
  `configkey` varchar(64) NOT NULL DEFAULT '',
  `configvalue` longtext DEFAULT NULL,
  PRIMARY KEY (`appid`,`configkey`)
);

CREATE INDEX `appconfig_config_key_index` ON oc_appconfig(`configkey`);

DROP TABLE IF EXISTS oc_filecache;
CREATE TABLE `oc_filecache` (
  `fileid` NUMERIC(20) NOT NULL AUTO_INCREMENT,
  `storage` NUMERIC(20) NOT NULL DEFAULT 0,
  `path` varchar(4000) DEFAULT NULL,
  `path_hash` varchar(32) NOT NULL DEFAULT '',
  `parent` NUMERIC(20) NOT NULL DEFAULT 0,
  `name` varchar(250) DEFAULT NULL,
  `mimetype` NUMERIC(20) NOT NULL DEFAULT 0,
  `mimepart` NUMERIC(20) NOT NULL DEFAULT 0,
  `size` NUMERIC(20) NOT NULL DEFAULT 0,
  `mtime` NUMERIC(20) NOT NULL DEFAULT 0,
  `storage_mtime` NUMERIC(20) NOT NULL DEFAULT 0,
  `encrypted` INTEGER NOT NULL DEFAULT 0,
  `unencrypted_size` NUMERIC(20) NOT NULL DEFAULT 0,
  `etag` varchar(40) DEFAULT NULL,
  `permissions` INTEGER DEFAULT 0,
  `checksum` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`fileid`)
);
CREATE UNIQUE INDEX `fs_storage_path_hash` ON oc_filecache(`storage`,`path_hash`);
CREATE INDEX `fs_parent_name_hash` ON oc_filecache(`parent`,`name`);
CREATE INDEX `fs_storage_mimetype` ON oc_filecache(`storage`,`mimetype`);
CREATE INDEX `fs_storage_mimepart` ON oc_filecache(`storage`,`mimepart`);
CREATE INDEX `fs_storage_size` ON oc_filecache(`storage`,`size`,`fileid`);
CREATE INDEX `fs_id_storage_size` ON oc_filecache(`fileid`,`storage`,`size`);
CREATE INDEX `fs_parent` ON oc_filecache(`parent`);
CREATE INDEX `fs_mtime` ON oc_filecache(`mtime`);
CREATE INDEX `fs_size` ON oc_filecache(`size`);
CREATE INDEX `fs_storage_path_prefix` ON oc_filecache(`storage`,`path`);

DROP TABLE IF EXISTS oc_group_folders;
CREATE TABLE `oc_group_folders` (
  `folder_id` NUMERIC(20) NOT NULL AUTO_INCREMENT,
  `mount_point` varchar(4000) NOT NULL,
  `quota` NUMERIC(20) NOT NULL DEFAULT -3,
  `acl` INTEGER DEFAULT 0,
  PRIMARY KEY (`folder_id`)
);

DROP TABLE IF EXISTS oc_mimetypes;
CREATE TABLE `oc_mimetypes` (
  `id` INTEGER NOT NULL AUTO_INCREMENT,
  `mimetype` varchar(255) NOT NULL DEFAULT '',
  PRIMARY KEY (`id`)
);

CREATE UNIQUE INDEX `mimetype_id_index` ON oc_mimetypes(`mimetype`);

DROP TABLE IF EXISTS oc_mounts;
CREATE TABLE `oc_mounts` (
  `id` NUMERIC(20) NOT NULL AUTO_INCREMENT,
  `storage_id` NUMERIC(20) NOT NULL,
  `root_id` NUMERIC(20) NOT NULL,
  `user_id` varchar(64) NOT NULL,
  `mount_point` varchar(4000) NOT NULL,
  `mount_id` NUMERIC(20) DEFAULT NULL,
  `mount_provider_class` varchar(128) DEFAULT NULL,
  PRIMARY KEY (`id`)
);
CREATE INDEX `mounts_storage_index` ON oc_mounts(`storage_id`);
CREATE INDEX `mounts_root_index` ON oc_mounts(`root_id`);
CREATE INDEX `mounts_mount_id_index` ON oc_mounts(`mount_id`);
CREATE INDEX `mounts_user_root_path_index` ON oc_mounts(`user_id`,`root_id`,`mount_point`);
CREATE INDEX `mounts_class_index` ON oc_mounts(`mount_provider_class`);
CREATE INDEX `mount_user_storage` ON oc_mounts(`storage_id`,`user_id`);
