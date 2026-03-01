# 数据库文件管理规范

## 文件存放位置

- 【强制】所有SQL文件统一存放在 `personal-ledger-backend/db/` 文件夹下
- 【强制】禁止将SQL文件分散在各个模块的resources目录中

## 文件命名规范

- 【强制】数据库初始化脚本命名为：`schema.sql`
- 【强制】初始数据脚本命名为：`data.sql`
- 【强制】版本升级脚本命名格式：`V{版本号}__{描述}.sql`，例如：`V1.0.1__add_user_table.sql`
- 【推荐】使用小写字母和下划线命名SQL文件

## 文件内容规范

- 【强制】每个SQL文件开头必须注明用途、作者和日期
- 【强制】建表语句必须包含 `IF NOT EXISTS` 判断
- 【强制】必须指定数据库字符集为 `utf8mb4`
- 【强制】表必备字段：`id`、`creator_code`、`updater_code`、`creator_name`、`updater_name`、`create_time`、`update_time`、`deleted`
- 【强制】建表语句必须使用 BaseEntity 中的模板字段，保证与实体类一致
- 【推荐】为重要字段添加索引和注释

## 示例

```sql
-- 用途：初始化数据库表结构
-- 作者：personal-ledger
-- 日期：2025-01-13

CREATE DATABASE IF NOT EXISTS personal_ledger_v2 
DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE personal_ledger_v2;

CREATE TABLE IF NOT EXISTS t_ledger (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    -- 业务字段...
    type VARCHAR(20) NOT NULL COMMENT '类型',
    amount DECIMAL(10, 2) NOT NULL COMMENT '金额',
    -- BaseEntity 公共字段
    creator_code VARCHAR(50) COMMENT '创建人编码',
    updater_code VARCHAR(50) COMMENT '更新人编码',
    creator_name VARCHAR(50) COMMENT '创建人姓名',
    updater_name VARCHAR(50) COMMENT '更新人姓名',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted VARCHAR(1) DEFAULT '0' COMMENT '逻辑删除标识：0-未删除，1-已删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='账单表';
```

## 目录结构

```
personal-ledger-v2/
└── personal-ledger-backend/
    └── db/
        ├── schema.sql              # 数据库表结构
        ├── data.sql                # 初始数据
        └── V1.0.1__description.sql # 版本升级脚本
```
