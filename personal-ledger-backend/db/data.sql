-- 用途：数据清洗规则 & 分类预设数据
-- 作者：personal-ledger
-- 日期：2025-01-13

USE personal_ledger_v2;

-- ============================================
-- 默认分类规则（必须配置）
-- ============================================
INSERT INTO bill_data_clean_rule (rule_type, match_fields, target_value, priority, enabled, remark) VALUES
('CATEGORY', '{"transaction_type":"EXPENSE"}', '其他支出', 0, '1', '支出默认分类'),
('CATEGORY', '{"transaction_type":"INCOME"}', '其他收入', 0, '1', '收入默认分类');

-- ============================================
-- 预设支出分类
-- ============================================
INSERT INTO bill_category (category_name, category_type, parent_id, icon, enabled, sort_order) VALUES
-- 一级分类
('餐饮', 'EXPENSE', NULL, '🍔', '1', 1),
('交通', 'EXPENSE', NULL, '🚗', '1', 2),
('购物', 'EXPENSE', NULL, '🛒', '1', 3),
('娱乐', 'EXPENSE', NULL, '🎮', '1', 4),
('住房', 'EXPENSE', NULL, '🏠', '1', 5),
('医疗', 'EXPENSE', NULL, '💊', '1', 6),
('教育', 'EXPENSE', NULL, '📚', '1', 7),
('其他支出', 'EXPENSE', NULL, '💰', '1', 8);

-- 获取刚插入的 ID（不同数据库可能语法不同，这里使用 MySQL 的方式）
SET @canyin_id = LAST_INSERT_ID();

INSERT INTO bill_category (category_name, category_type, parent_id, icon, enabled, sort_order) VALUES
('早餐', 'EXPENSE', @canyin_id, NULL, '1', 1),
('午餐', 'EXPENSE', @canyin_id, NULL, '1', 2),
('晚餐', 'EXPENSE', @canyin_id, NULL, '1', 3);

-- ============================================
-- 预设收入分类
-- ============================================
INSERT INTO bill_category (category_name, category_type, parent_id, icon, enabled, sort_order) VALUES
('工资', 'INCOME', NULL, '💰', '1', 1),
('奖金', 'INCOME', NULL, '🎁', '1', 2),
('投资收益', 'INCOME', NULL, '📈', '1', 3),
('其他收入', 'INCOME', NULL, '💵', '1', 4);
