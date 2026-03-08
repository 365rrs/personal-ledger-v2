-- 用途：数据清洗规则初始化数据
-- 作者：personal-ledger
-- 日期：2026-03-08

USE personal_ledger_v2;

-- 支付渠道映射
INSERT INTO bill_data_clean_rule (rule_type, match_fields, target_value, priority, enabled, remark) VALUES
('PAYMENT_CHANNEL', '{"payment_channel":"微信"}', '微信支付', 100, '1', '微信'),
('PAYMENT_CHANNEL', '{"payment_channel":"支付宝"}', '支付宝', 100, '1', '支付宝'),
('PAYMENT_CHANNEL', '{"payment_channel":"工行"}', '工商银行', 100, '1', '工商银行'),
('PAYMENT_CHANNEL', '{"payment_channel":"建行"}', '建设银行', 100, '1', '建设银行'),
('PAYMENT_CHANNEL', '{"payment_channel":"农行"}', '农业银行', 100, '1', '农业银行'),
('PAYMENT_CHANNEL', '{"payment_channel":"中行"}', '中国银行', 100, '1', '中国银行'),
('PAYMENT_CHANNEL', '{"payment_channel":"现金"}', '现金', 100, '1', '现金'),
('PAYMENT_CHANNEL', '{"payment_channel":"银行卡"}', '银行卡', 100, '1', '银行卡');

-- 分类映射 - 支出
INSERT INTO bill_data_clean_rule (rule_type, match_fields, target_value, priority, enabled, remark) VALUES
('CATEGORY', '{"category":"吃饭","transaction_type":"EXPENSE"}', '餐饮', 100, '1', '餐饮'),
('CATEGORY', '{"category":"早餐","transaction_type":"EXPENSE"}', '餐饮', 100, '1', '餐饮'),
('CATEGORY', '{"category":"午餐","transaction_type":"EXPENSE"}', '餐饮', 100, '1', '餐饮'),
('CATEGORY', '{"category":"晚餐","transaction_type":"EXPENSE"}', '餐饮', 100, '1', '餐饮'),
('CATEGORY', '{"category":"打车","transaction_type":"EXPENSE"}', '交通', 100, '1', '交通'),
('CATEGORY', '{"category":"地铁","transaction_type":"EXPENSE"}', '交通', 100, '1', '交通'),
('CATEGORY', '{"category":"公交","transaction_type":"EXPENSE"}', '交通', 100, '1', '交通'),
('CATEGORY', '{"category":"购物","transaction_type":"EXPENSE"}', '购物', 100, '1', '购物'),
('CATEGORY', '{"category":"超市","transaction_type":"EXPENSE"}', '购物', 100, '1', '购物'),
('CATEGORY', '{"category":"网购","transaction_type":"EXPENSE"}', '购物', 100, '1', '购物');

-- 分类映射 - 收入
INSERT INTO bill_data_clean_rule (rule_type, match_fields, target_value, priority, enabled, remark) VALUES
('CATEGORY', '{"category":"工资","transaction_type":"INCOME"}', '工资收入', 100, '1', '工资'),
('CATEGORY', '{"category":"奖金","transaction_type":"INCOME"}', '奖金收入', 100, '1', '奖金'),
('CATEGORY', '{"category":"兼职","transaction_type":"INCOME"}', '兼职收入', 100, '1', '兼职');

-- 默认分类
INSERT INTO bill_data_clean_rule (rule_type, match_fields, target_value, priority, enabled, remark) VALUES
('CATEGORY', '{"transaction_type":"EXPENSE"}', '其他支出', 0, '1', '支出默认分类'),
('CATEGORY', '{"transaction_type":"INCOME"}', '其他收入', 0, '1', '收入默认分类');

-- 备注映射
INSERT INTO bill_data_clean_rule (rule_type, match_fields, target_value, priority, enabled, remark) VALUES
('TRANSACTION_DESC', '{"transaction_desc":"美团外卖"}', '外卖订单', 100, '1', '美团外卖'),
('TRANSACTION_DESC', '{"transaction_desc":"饿了么"}', '外卖订单', 100, '1', '饿了么'),
('TRANSACTION_DESC', '{"transaction_desc":"滴滴出行"}', '打车费用', 100, '1', '滴滴出行'),
('TRANSACTION_DESC', '{"transaction_desc":"微信支付"}', '微信支付', 50, '1', '微信支付备注'),
('TRANSACTION_DESC', '{"transaction_desc":"支付宝"}', '支付宝支付', 50, '1', '支付宝备注');
