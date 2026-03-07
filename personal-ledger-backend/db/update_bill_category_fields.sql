-- ============================================
-- 账单表分类字段升级脚本
-- 功能：添加 categoryId 和 subCategoryId 字段，同时保留原有的 category 和 sub_category 字段
-- 说明：新旧字段共存，保证向后兼容
-- ============================================

-- 添加新字段
ALTER TABLE bill ADD COLUMN category_id BIGINT COMMENT '分类 ID';
ALTER TABLE bill ADD COLUMN sub_category_id BIGINT COMMENT '二级分类 ID';

-- 迁移旧数据到新字段（根据分类名称匹配 ID）
UPDATE bill b
INNER JOIN bill_category bc ON b.category = bc.category_name
SET b.category_id = bc.id
WHERE b.category IS NOT NULL;

UPDATE bill b
INNER JOIN bill_category bc ON b.sub_category = bc.category_name
SET b.sub_category_id = bc.id
WHERE b.sub_category IS NOT NULL;

-- 添加新索引
ALTER TABLE bill ADD INDEX idx_category_id (category_id);
ALTER TABLE bill ADD INDEX idx_sub_category_id (sub_category_id);
