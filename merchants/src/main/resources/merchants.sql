CREATE TABLE `merchants` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(64) COLLATE utf8_bin NOT NULL COMMENT '商户名称',
  `logo_url` varchar(256) COLLATE utf8_bin NOT NULL COMMENT '商户 logo',
  `business_license_url` VARCHAR(256) COLLATE utf8_bin NOT NULL COMMENT '商户营业执照',
  `phone` varchar(64) COLLATE utf8_bin NOT NULL COMMENT '商户联系电话',
  `address`  varchar(64) COLLATE utf8_bin NOT NULL COMMENT '商户地址',
  `is_audit` BOOLEAN NOT NULL COMMENT '是否通过审核',
  PRIMARY KEY (`id`)
#      设置第一个id为17，具体没啥用
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET =utf8;
