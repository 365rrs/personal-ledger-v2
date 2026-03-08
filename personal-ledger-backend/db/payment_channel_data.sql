-- ============================================
-- 支付渠道预设数据
-- ============================================

-- 现金类
INSERT INTO bill_payment_channel (channel_name, channel_type, icon, enabled, sort_order) 
VALUES ('现金', 'CASH', '💵', '1', 1);

-- 银行卡类
INSERT INTO bill_payment_channel (channel_name, channel_type, icon, enabled, sort_order) 
VALUES ('工商银行', 'BANK_CARD', '🏦', '1', 2);

INSERT INTO bill_payment_channel (channel_name, channel_type, icon, enabled, sort_order) 
VALUES ('建设银行', 'BANK_CARD', '🏦', '1', 3);

INSERT INTO bill_payment_channel (channel_name, channel_type, icon, enabled, sort_order) 
VALUES ('农业银行', 'BANK_CARD', '🏦', '1', 4);

INSERT INTO bill_payment_channel (channel_name, channel_type, icon, enabled, sort_order) 
VALUES ('中国银行', 'BANK_CARD', '🏦', '1', 5);

INSERT INTO bill_payment_channel (channel_name, channel_type, icon, enabled, sort_order) 
VALUES ('招商银行', 'BANK_CARD', '🏦', '1', 6);

-- 信用卡类
INSERT INTO bill_payment_channel (channel_name, channel_type, icon, enabled, sort_order) 
VALUES ('信用卡', 'CREDIT_CARD', '💳', '1', 7);

-- 电子钱包类
INSERT INTO bill_payment_channel (channel_name, channel_type, icon, enabled, sort_order) 
VALUES ('微信支付', 'E_WALLET', '💚', '1', 8);

INSERT INTO bill_payment_channel (channel_name, channel_type, icon, enabled, sort_order) 
VALUES ('支付宝', 'E_WALLET', '💙', '1', 9);

INSERT INTO bill_payment_channel (channel_name, channel_type, icon, enabled, sort_order) 
VALUES ('云闪付', 'E_WALLET', '🟡', '1', 10);

-- 其他
INSERT INTO bill_payment_channel (channel_name, channel_type, icon, enabled, sort_order) 
VALUES ('其他渠道', 'OTHER', '⚫', '1', 11);
