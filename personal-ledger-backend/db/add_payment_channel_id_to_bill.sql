-- 为 bill 表添加 payment_channel_id 字段
ALTER TABLE bill ADD COLUMN `payment_channel_id` bigint DEFAULT NULL COMMENT '支付渠道 ID' AFTER `payment_channel`;

-- 根据现有的 payment_channel 名称，批量更新 payment_channel_id
UPDATE bill b
INNER JOIN bill_payment_channel pc ON b.payment_channel = pc.channel_name
SET b.payment_channel_id = pc.id
WHERE b.payment_channel IS NOT NULL;
