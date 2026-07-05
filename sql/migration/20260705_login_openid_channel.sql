-- 登录模块调整（login.md）
-- 1) user 表新增 openid / unionid / channel / role 字段
-- 2) 迁移历史数据：account = 'wx_{openid}' 抽取到 openid 列，并将 channel 置为 mini

ALTER TABLE `user`
    ADD COLUMN `openid`  VARCHAR(64)  DEFAULT NULL COMMENT '微信小程序 openid' AFTER `account`,
    ADD COLUMN `unionid` VARCHAR(64)  DEFAULT NULL COMMENT '微信 unionid'      AFTER `openid`,
    ADD COLUMN `channel` VARCHAR(16)  NOT NULL DEFAULT 'admin' COMMENT '来源渠道 mini/admin' AFTER `is_admin`,
    ADD COLUMN `role`    VARCHAR(32)  NOT NULL DEFAULT 'user'  COMMENT '业务角色 user/admin' AFTER `channel`,
    ADD UNIQUE KEY `uk_openid` (`openid`);

UPDATE `user`
   SET `openid`  = SUBSTRING(`account`, 4),
       `channel` = 'mini',
       `role`    = 'user'
 WHERE `account` LIKE 'wx\_%' ESCAPE '\\';

UPDATE `user`
   SET `role` = 'admin'
 WHERE `is_admin` = 1;
