-- 添加用户信用分字段到用户表
ALTER TABLE user_info ADD COLUMN creditScore INT NOT NULL DEFAULT 100 COMMENT '用户信用分(0-100)';