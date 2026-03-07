-- 检查 bill_category 表中是否存在重复的 sortOrder
SELECT sort_order, COUNT(*) as count
FROM bill_category
WHERE deleted = '0'
GROUP BY sort_order
HAVING COUNT(*) > 1;

-- 如果存在重复的 sortOrder，执行以下脚本重新生成连续的排序序号
-- 注意：这只是一个临时修复脚本，正常情况下应该通过拖拽功能自动修复

-- 为支出分类重新生成排序序号（按 ID 升序）
SET @row_number = 0;
UPDATE bill_category
SET sort_order = (@row_number := @row_number + 1)
WHERE deleted = '0' 
  AND category_type = 'EXPENSE'
ORDER BY sort_order, id;

-- 为收入分类重新生成排序序号（按 ID 升序）
SET @row_number = 0;
UPDATE bill_category
SET sort_order = (@row_number := @row_number + 1)
WHERE deleted = '0' 
  AND category_type = 'INCOME'
ORDER BY sort_order, id;
