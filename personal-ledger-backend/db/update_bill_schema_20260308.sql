-- 用途：更新 bill 表结构 - 添加分类 ID 字段
-- 作者：personal-ledger
-- 日期：2026-03-08

USE personal_ledger_v2;

-- ============================================
-- 1. 为 bill 表添加分类 ID 字段
-- ============================================
ALTER TABLE bill 
ADD COLUMN category_id BIGINT COMMENT '分类 ID' AFTER category,
ADD COLUMN sub_category_id BIGINT COMMENT '二级分类 ID' AFTER sub_category;

-- ============================================
-- 2. 为 bill 表添加索引
-- ============================================
ALTER TABLE bill 
ADD INDEX idx_category_id (category_id),
ADD INDEX idx_sub_category_id (sub_category_id);

-- ============================================
-- 3. 修改 amount_type 字段默认值
-- ============================================
ALTER TABLE bill 
MODIFY COLUMN amount_type VARCHAR(20) NOT NULL DEFAULT 'EXPENSE' COMMENT '金额类型：INCOME-收入，EXPENSE-支出';

-- ============================================
-- 4. 修改 category 字段允许 NULL
-- ============================================
ALTER TABLE bill 
MODIFY COLUMN category VARCHAR(50) COMMENT '分类';

-- ============================================
-- 5. 为 bill_tag 表添加 tag_status 字段
-- ============================================
ALTER TABLE bill_tag 
ADD COLUMN tag_status VARCHAR(10) DEFAULT 'enable' COMMENT '状态：enable-启用，disable-停用' AFTER sort_order;

-- ============================================
-- 6. 为 bill_import_detail 表添加/修改字段
-- ============================================
-- 如果存在 type 字段，先重命名为 amount_type
-- 注意：这个操作需要根据实际情况执行
-- ALTER TABLE bill_import_detail CHANGE COLUMN `type` `amount_type` VARCHAR(20) COMMENT '金额类型（INCOME/EXPENSE）';

-- 添加 transaction_type 字段（如果不存在）
-- ALTER TABLE bill_import_detail ADD COLUMN transaction_type VARCHAR(100) COMMENT '交易类型（原始值）' AFTER amount;

-- ============================================
-- 说明：
-- 1. category_id 和 sub_category_id 用于关联 bill_category 表
-- 2. 索引 idx_category_id 和 idx_sub_category_id 用于优化查询性能
-- 3. tag_status 字段用于控制标签的启用/停用状态
-- ============================================
