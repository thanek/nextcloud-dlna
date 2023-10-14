
INSERT INTO `oc_appconfig` VALUES
('groupfolders','enabled','yes'),
('groupfolders','installed_version','15.3.1'),
('groupfolders','types','filesystem,dav');

INSERT INTO `oc_filecache` VALUES
(1,1,'','d41d8cd98f00b204e9800998ecf8427e',-1,'',2,1,5341566992,1696704138,1696613362,0,0,'',23,''),
(2,1,'files','45b963397aa40d4a0063e0d85e4fe7a1',1,'files',2,1,5341560694,1696704138,1696704138,0,0,'',31,''),
(3,2,'','d41d8cd98f00b204e9800998ecf8427e',-1,'',2,1,13286908,1696702221,1696695204,0,0,'',23,''),
(4,2,'appdata_integration','bed7fa8a60170b5d88c9da5e69eaeb5a',3,'appdata_integration',2,1,10274496,1695737790,1695737790,0,0,'',31,''),
(13,1,'files/Nextcloud intro.mp4','e4919345bcc87d4585a5525daaad99c0',2,'Nextcloud intro.mp4',9,8,3963036,1695737656,1695737656,0,0,'',27,''),
(14,1,'files/Nextcloud.png','',2,'Nextcloud.png',11,10,50598,1695737656,1695737656,0,0,'',27,''),
(15,1,'files/Photos','d01bb67e7b71dd49fd06bad922f521c9',2,'Photos',2,1,5656462,1695737827,1695737658,0,0,'',31,''),
(16,1,'files/Photos/Birdie.jpg','cd31c7af3a0ec6e15782b5edd2774549',15,'Birdie.jpg',12,10,593508,1695737656,1695737656,0,0,'',27,''),
(17,1,'files/Photos/Frog.jpg','d6219add1a9129ed0c1513af985e2081',15,'Frog.jpg',12,10,457744,1695737656,1695737656,0,0,'',27,''),
(18,1,'files/Photos/Gorilla.jpg','6d5f5956d8ff76a5f290cebb56402789',15,'Gorilla.jpg',12,10,474653,1695737656,1695737656,0,0,'',27,''),
(19,1,'files/Photos/Library.jpg','0b785d02a19fc00979f82f6b54a05805',15,'Library.jpg',12,10,2170375,1695737657,1695737657,0,0,'',27,''),
(20,1,'files/Photos/Nextcloud community.jpg','b9b3caef83a2a1c20354b98df6bcd9d0',15,'Nextcloud community.jpg',12,10,797325,1695737657,1695737657,0,0,
'',27,''),
(22,1,'files/Photos/Steps.jpg','7b2ca8d05bbad97e00cbf5833d43e912',15,'Steps.jpg',12,10,567689,1695737658,1695737658,0,0,'',27,''),
(23,1,'files/Photos/Toucan.jpg','681d1e78f46a233e12ecfa722cbc2aef',15,'Toucan.jpg',12,10,167989,1695737658,1695737658,0,0,'',27,''),
(24,1,'files/Photos/Vineyard.jpg','14e5f2670b0817614acd52269d971db8',15,'Vineyard.jpg',12,10,427030,1695737658,1695737658,0,0,'',27,''),
(69,2,'appdata_integration/preview','e771733d5f59ead277f502588282d693',4,'preview',2,1,5153144,1695738765,1695738765,0,0,'',31,'');

INSERT INTO `oc_group_folders` VALUES
(1,'family folder',-3,0);

INSERT INTO `oc_mimetypes` VALUES
(5,'application'),
(19,'application/gzip'),
(18,'application/javascript'),
(20,'application/json'),
(16,'application/octet-stream'),
(6,'application/pdf'),
(13,'application/vnd.oasis.opendocument.graphics'),
(15,'application/vnd.oasis.opendocument.presentation'),
(14,'application/vnd.oasis.opendocument.spreadsheet'),
(17,'application/vnd.oasis.opendocument.text'),
(7,'application/vnd.openxmlformats-officedocument.wordprocessingml.document'),
(22,'audio'),
(23,'audio/mpeg'),
(1,'httpd'),
(2,'httpd/unix-directory'),
(10,'image'),
(12,'image/jpeg'),
(11,'image/png'),
(21,'image/svg+xml'),
(3,'text'),
(4,'text/markdown'),
(8,'video'),
(9,'video/mp4');

INSERT INTO `oc_mounts` VALUES
(1,1,1,'johndoe','/johndoe/',NULL,'OC\\Files\\Mount\\LocalHomeMountProvider'),
(2,3,384,'janedoe','/janedoe/',NULL,'OC\\Files\\Mount\\LocalHomeMountProvider'),
(3,2,586,'johndoe','/johndoe/files/family folder/',NULL,'OCA\\GroupFolders\\Mount\\MountProvider'),
(4,2,586,'janedoe','/janedoe/files/family folder/',NULL,'OCA\\GroupFolders\\Mount\\MountProvider');
