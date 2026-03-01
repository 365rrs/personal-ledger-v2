-- 用途：初始化数据库表结构
-- 作者：personal-ledger
-- 日期：2025-01-13

-- 创建数据库
CREATE DATABASE IF NOT EXISTS personal_ledger_v2 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE personal_ledger_v2;

-- 创建账单表
CREATE TABLE IF NOT EXISTS t_ledger (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    type VARCHAR(20) NOT NULL COMMENT '类型：INCOME-收入，EXPENSE-支出',
    amount DECIMAL(10, 2) NOT NULL COMMENT '金额',
    category VARCHAR(50) NOT NULL COMMENT '分类',
    description VARCHAR(200) COMMENT '描述',
    transaction_time DATETIME NOT NULL COMMENT '交易时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT UNSIGNED DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    INDEX idx_type (type),
    INDEX idx_transaction_time (transaction_time),
    INDEX idx_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='账单表';
