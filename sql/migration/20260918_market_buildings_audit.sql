-- 车位租售闭环补全（P0）——幂等迁移
-- 目标：
--   1) market 表确保存在 buildings 列（靠近楼栋，dict_item.value 逗号分隔）
--   2) market 表确保存在 audit_remark 列（审核驳回原因）
-- 说明：
--   MySQL 8.0 不支持 ADD COLUMN IF NOT EXISTS，故先查 information_schema，
--   缺失的列才执行 ADD，保证脚本可重复执行。

-- 1) 校验/补充 buildings 列
SET @has_buildings := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'market'
      AND COLUMN_NAME = 'buildings'
);

SET @sql_buildings := IF(
    @has_buildings = 0,
    'ALTER TABLE `market` ADD COLUMN `buildings` VARCHAR(255) DEFAULT NULL COMMENT ''靠近楼栋（dict_item.value，逗号分隔）'' AFTER `community`',
    'SELECT ''buildings column already exists'' AS note'
);
PREPARE stmt_buildings FROM @sql_buildings;
EXECUTE stmt_buildings;
DEALLOCATE PREPARE stmt_buildings;

-- 2) 校验/补充 audit_remark 列
SET @has_audit_remark := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'market'
      AND COLUMN_NAME = 'audit_remark'
);

SET @sql_audit_remark := IF(
    @has_audit_remark = 0,
    'ALTER TABLE `market` ADD COLUMN `audit_remark` VARCHAR(255) DEFAULT NULL COMMENT ''审核驳回原因'' AFTER `audit_time`',
    'SELECT ''audit_remark column already exists'' AS note'
);
PREPARE stmt_audit_remark FROM @sql_audit_remark;
EXECUTE stmt_audit_remark;
DEALLOCATE PREPARE stmt_audit_remark;
