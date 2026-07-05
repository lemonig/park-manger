-- TablePro SQL Export
-- Generated: 2026-06-20T03:33:54Z
-- Database Type: MySQL

-- --------------------------------------------------------
-- Table: carport
-- --------------------------------------------------------

CREATE TABLE `carport` (
  `id` int NOT NULL AUTO_INCREMENT,
  `number` varchar(10) NOT NULL COMMENT '车位编号',
  `description` varchar(255) DEFAULT NULL,
  `photo` varchar(255) DEFAULT NULL COMMENT '照片',
  `gmt_create` datetime DEFAULT CURRENT_TIMESTAMP,
  `gmt_modify` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_number` (`number`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- --------------------------------------------------------
-- Table: dict_type
-- --------------------------------------------------------

CREATE TABLE `dict_type` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `code` varchar(100) NOT NULL,
  `name` varchar(100) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `status` tinyint DEFAULT '1',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- --------------------------------------------------------
-- Table: images
-- --------------------------------------------------------

CREATE TABLE `images` (
  `id` varchar(100) NOT NULL COMMENT '文件id',
  `url` varchar(512) NOT NULL,
  `size` bigint DEFAULT NULL,
  `filename` varchar(256) DEFAULT NULL,
  `original_filename` varchar(256) DEFAULT NULL,
  `base_path` varchar(256) DEFAULT NULL,
  `path` varchar(256) DEFAULT NULL,
  `ext` varchar(32) DEFAULT NULL,
  `content_type` varchar(256) DEFAULT NULL,
  `platform` varchar(32) DEFAULT NULL,
  `th_url` varchar(512) DEFAULT NULL,
  `th_filename` varchar(256) DEFAULT NULL,
  `th_size` bigint DEFAULT NULL,
  `th_content_type` varchar(32) DEFAULT NULL,
  `object_id` varchar(100) DEFAULT NULL,
  `object_type` varchar(32) DEFAULT NULL,
  `attr` text,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- --------------------------------------------------------
-- Table: market
-- --------------------------------------------------------

CREATE TABLE `market` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `code` varchar(100) NOT NULL,
  `user_id` int DEFAULT NULL,
  `price` decimal(10,2) NOT NULL,
  `parking_no` varchar(50) NOT NULL,
  `description` text,
  `phone` varchar(20) NOT NULL,
  `position_desc` varchar(255) DEFAULT NULL,
  `specs` varchar(255) DEFAULT NULL,
  `status` tinyint NOT NULL DEFAULT '0',
  `audit_time` datetime DEFAULT NULL,
  `city` varchar(50) DEFAULT NULL,
  `area` varchar(50) DEFAULT NULL,
  `community` varchar(100) DEFAULT NULL,
  `type` tinyint NOT NULL COMMENT '1=售卖，2=租赁',
  `gmt_create` datetime DEFAULT CURRENT_TIMESTAMP,
  `gmt_modify` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`),
  KEY `idx_status` (`status`),
  KEY `idx_audit_time` (`audit_time`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- --------------------------------------------------------
-- Table: user
-- --------------------------------------------------------

CREATE TABLE `user` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL,
  `doorplate` varchar(50) NOT NULL,
  `account` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '账号',
  `openid` varchar(64) DEFAULT NULL COMMENT '微信小程序 openid',
  `unionid` varchar(64) DEFAULT NULL COMMENT '微信 unionid',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '123456',
  `is_admin` tinyint NOT NULL DEFAULT '0' COMMENT '是否超级管理员',
  `channel` varchar(16) NOT NULL DEFAULT 'admin' COMMENT '来源渠道 mini/admin',
  `role` varchar(32) NOT NULL DEFAULT 'user' COMMENT '业务角色 user/admin',
  `mobile` varchar(20) DEFAULT NULL,
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '头像',
  `description` varchar(255) DEFAULT NULL,
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `gmt_modify` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `gmt_active` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_account` (`account`),
  UNIQUE KEY `uk_openid` (`openid`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- --------------------------------------------------------
-- Table: user_login
-- --------------------------------------------------------

CREATE TABLE `user_login` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `type` tinyint DEFAULT NULL COMMENT '1=web 2=mobile',
  `token` varchar(255) NOT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `expire_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_token` (`token`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- --------------------------------------------------------
-- Table: dict_item
-- --------------------------------------------------------

CREATE TABLE `dict_item` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `type_code` varchar(100) NOT NULL,
  `label` varchar(100) NOT NULL,
  `value` varchar(100) NOT NULL,
  `parent_value` varchar(100) DEFAULT NULL,
  `sort` int DEFAULT '0',
  `status` tinyint DEFAULT '1',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_parent_value` (`parent_value`),
  KEY `idx_type_sort` (`type_code`,`sort`),
  CONSTRAINT `fk_dict_type` FOREIGN KEY (`type_code`) REFERENCES `dict_type` (`code`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- --------------------------------------------------------
-- Table: market_images
-- --------------------------------------------------------

CREATE TABLE `market_images` (
  `id` int NOT NULL AUTO_INCREMENT,
  `market_code` varchar(100) NOT NULL,
  `image_id` varchar(100) NOT NULL,
  `create_by` int DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_market_code` (`market_code`),
  KEY `idx_image_id` (`image_id`),
  CONSTRAINT `fk_image_id` FOREIGN KEY (`image_id`) REFERENCES `images` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_market_code` FOREIGN KEY (`market_code`) REFERENCES `market` (`code`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- --------------------------------------------------------
-- Table: carport_user
-- --------------------------------------------------------

CREATE TABLE `carport_user` (
  `id` int NOT NULL AUTO_INCREMENT,
  `carport_id` int NOT NULL,
  `user_id` int NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_carport_user` (`carport_id`,`user_id`),
  KEY `idx_user_id` (`user_id`),
  CONSTRAINT `fk_carport` FOREIGN KEY (`carport_id`) REFERENCES `carport` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

ALTER TABLE `park`.`dict_item` ADD CONSTRAINT `fk_dict_type` FOREIGN KEY (`type_code`) REFERENCES `park`.`dict_type` (`code`);
ALTER TABLE `park`.`market_images` ADD CONSTRAINT `fk_image_id` FOREIGN KEY (`image_id`) REFERENCES `park`.`images` (`id`) ON DELETE CASCADE;
ALTER TABLE `park`.`market_images` ADD CONSTRAINT `fk_market_code` FOREIGN KEY (`market_code`) REFERENCES `park`.`market` (`code`) ON DELETE CASCADE;
ALTER TABLE `park`.`carport_user` ADD CONSTRAINT `fk_carport` FOREIGN KEY (`carport_id`) REFERENCES `park`.`carport` (`id`) ON DELETE CASCADE;
ALTER TABLE `park`.`carport_user` ADD CONSTRAINT `fk_user` FOREIGN KEY (`user_id`) REFERENCES `park`.`user` (`id`) ON DELETE CASCADE;

