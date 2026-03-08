-- 用途：测试数据清洗规则
-- 作者：personal-ledger
-- 日期：2026-03-08

USE personal_ledger_v2;

-- 1. 查询所有清洗规则
SELECT * FROM bill_data_clean_rule 
WHERE deleted = '0' 
ORDER BY rule_type, priority DESC;

-- 2. 测试支付渠道清洗
SELECT target_value 
FROM bill_data_clean_rule 
WHERE rule_type = 'PAYMENT_CHANNEL'
  AND JSON_EXTRACT(match_fields, '$.payment_channel') = '微信'
  AND enabled = '1' 
  AND deleted = '0'
ORDER BY priority DESC
LIMIT 1;

-- 3. 测试分类清洗（吃饭 + EXPENSE → 餐饮）
SELECT target_value 
FROM bill_data_clean_rule 
WHERE rule_type = 'CATEGORY'
  AND JSON_EXTRACT(match_fields, '$.category') = '吃饭'
  AND JSON_EXTRACT(match_fields, '$.transaction_type') = 'EXPENSE'
  AND enabled = '1' 
  AND deleted = '0'
ORDER BY priority DESC
LIMIT 1;

-- 4. 测试默认分类（EXPENSE → 其他支出）
SELECT target_value 
FROM bill_data_clean_rule 
WHERE rule_type = 'CATEGORY'
  AND JSON_LENGTH(match_fields) = 1
  AND JSON_EXTRACT(match_fields, '$.transaction_type') = 'EXPENSE'
  AND enabled = '1' 
  AND deleted = '0'
ORDER BY priority DESC
LIMIT 1;

-- 5. 统计各类型规则数量
SELECT 
  rule_type,
  COUNT(*) as rule_count,
  SUM(CASE WHEN enabled = '1' THEN 1 ELSE 0 END) as enabled_count
FROM bill_data_clean_rule
WHERE deleted = '0'
GROUP BY rule_type;

-- 6. 查看优先级最高的前 10 条规则
SELECT 
  id,
  rule_type,
  match_fields,
  target_value,
  priority,
  enabled,
  remark
FROM bill_data_clean_rule
WHERE deleted = '0'
ORDER BY priority DESC
LIMIT 10;
