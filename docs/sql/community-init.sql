-- ============================================================
-- 迁移：小区库 + market.community_id
-- 背景：车位租售地址原先取自填报人实时 GPS 逆地理编码（city/area），
--       人不在小区时地址必错。改为「小区库 + 选择器」：标的地址以
--       受控小区字典为唯一来源，GPS 仅用于附近小区排序。
-- 执行方式：手动在目标库执行（项目未接 Flyway/Liquibase）。
-- ============================================================

-- 1. 小区字典表
CREATE TABLE `community` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL COMMENT '规范小区名',
  `province` varchar(50) DEFAULT NULL COMMENT '省',
  `city` varchar(50) DEFAULT NULL COMMENT '市',
  `area` varchar(50) DEFAULT NULL COMMENT '区/县',
  `address` varchar(255) DEFAULT NULL COMMENT '详细地址',
  `lat` decimal(10,6) DEFAULT NULL COMMENT '纬度（gcj02，用于附近排序/地图聚合）',
  `lng` decimal(10,6) DEFAULT NULL COMMENT '经度（gcj02）',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：1=启用，0=停用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_city_area` (`city`,`area`),
  KEY `idx_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='小区字典';

-- 2. market 关联小区（冗余的 city/area/community 文本由服务端按选中小区回写）
ALTER TABLE `market`
  ADD COLUMN `community_id` bigint DEFAULT NULL COMMENT '小区字典ID' AFTER `area`,
  ADD KEY `idx_community_id` (`community_id`);

-- 3. 存量数据回填：按小区名精确匹配挂接（匹配不上的保持 NULL，由运营在后台补录后人工修复）
UPDATE market m
  JOIN community c ON m.community = c.name
SET m.community_id = c.id
WHERE m.community_id IS NULL;
