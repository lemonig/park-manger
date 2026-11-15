-- ============================================================
-- 迁移：车位收藏表 market_favorite
-- 背景：首页车位列表新增收藏功能，用户可收藏感兴趣的车位并在
--       「我的-我的收藏」查看。数据存服务端，跨设备一致。
-- 设计：uk_user_market 保证幂等（接口用 INSERT IGNORE / DELETE，
--       项目无逻辑删除惯例，收藏取消即物理删除）；
--       user/market 均级联删除（用户注销或车位删除时收藏随之清理）。
-- 执行方式：手动在目标库执行（项目未接 Flyway/Liquibase）。
-- ============================================================

CREATE TABLE IF NOT EXISTS `market_favorite` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL COMMENT '收藏人，user.id',
  `market_id` bigint NOT NULL COMMENT '被收藏车位，market.id',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_market` (`user_id`,`market_id`),
  KEY `idx_user_id` (`user_id`),
  CONSTRAINT `fk_fav_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_fav_market` FOREIGN KEY (`market_id`) REFERENCES `market` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='车位收藏';
