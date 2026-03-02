-- 用途：个人账本系统数据库表结构
-- 作者：personal-ledger
-- 日期：2025-01-13

-- 创建数据库
CREATE DATABASE IF NOT EXISTS personal_ledger_v2 
DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE personal_ledger_v2;

-- ============================================
-- 1. bill_import_record（导入记录表）
-- ============================================
CREATE TABLE IF NOT EXISTS bill_import_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    file_name VARCHAR(200) NOT NULL COMMENT '导入文件名',
    file_size BIGINT COMMENT '文件大小（字节）',
    total_count INT NOT NULL DEFAULT 0 COMMENT '总记录数',
    processed_count INT NOT NULL DEFAULT 0 COMMENT '已处理记录数',
    success_count INT NOT NULL DEFAULT 0 COMMENT '成功导入数',
    fail_count INT NOT NULL DEFAULT 0 COMMENT '失败记录数',
    status VARCHAR(20) NOT NULL COMMENT '导入状态：PROCESSING-处理中，SUCCESS-成功，FAILED-失败，PARTIAL-部分成功',
    error_message TEXT COMMENT '错误信息',
    file_path VARCHAR(500) COMMENT '文件存储路径',
    start_time DATETIME NOT NULL COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    creator_code VARCHAR(50) COMMENT '创建人编码',
    updater_code VARCHAR(50) COMMENT '更新人编码',
    creator_name VARCHAR(50) COMMENT '创建人姓名',
    updater_name VARCHAR(50) COMMENT '更新人姓名',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted VARCHAR(1) DEFAULT '0' COMMENT '逻辑删除标识：0-未删除，1-已删除',
    INDEX idx_status (status),
    INDEX idx_start_time (start_time),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='导入记录表';

-- ============================================
-- 2. bill_import_detail（导入明细表）
-- ============================================
CREATE TABLE IF NOT EXISTS bill_import_detail (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    import_record_id BIGINT NOT NULL COMMENT '导入记录ID',
    original_data TEXT COMMENT '原始数据（JSON格式）',
    type VARCHAR(20) COMMENT '类型',
    amount DECIMAL(10, 2) COMMENT '金额',
    description VARCHAR(500) COMMENT '描述',
    transaction_time DATETIME COMMENT '交易时间',
    import_status VARCHAR(20) NOT NULL COMMENT '导入状态：SUCCESS-成功，FAILED-失败',
    duplicate_status VARCHAR(20) NOT NULL DEFAULT 'UNCHECKED' COMMENT '重复状态：UNCHECKED-未检查，UNIQUE-唯一，DUPLICATE-重复',
    duplicate_ledger_id BIGINT COMMENT '重复的账单ID（如果是重复记录）',
    data_hash VARCHAR(64) COMMENT '数据指纹（用于重复检测）',
    convert_status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '转账单状态：PENDING-待转换，CONVERTED-已转换，SKIPPED-已跳过，DUPLICATE-重复跳过，CONVERT_FAILED-转换失败',
    convert_error_message VARCHAR(500) COMMENT '转换错误信息',
    error_message VARCHAR(500) COMMENT '错误信息',
    ledger_id BIGINT COMMENT '关联的账单ID（转换成功后）',
    creator_code VARCHAR(50) COMMENT '创建人编码',
    updater_code VARCHAR(50) COMMENT '更新人编码',
    creator_name VARCHAR(50) COMMENT '创建人姓名',
    updater_name VARCHAR(50) COMMENT '更新人姓名',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted VARCHAR(1) DEFAULT '0' COMMENT '逻辑删除标识：0-未删除，1-已删除',
    INDEX idx_import_record_id (import_record_id),
    INDEX idx_import_status (import_status),
    INDEX idx_duplicate_status (duplicate_status),
    INDEX idx_data_hash (data_hash),
    INDEX idx_convert_status (convert_status),
    INDEX idx_ledger_id (ledger_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='导入明细表';

-- ============================================
-- 3. bill（账单表）
-- ============================================
CREATE TABLE IF NOT EXISTS bill (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    transaction_date DATE NOT NULL COMMENT '交易日期',
    transaction_time TIME COMMENT '交易时间',
    income_amount DECIMAL(10, 2) DEFAULT 0.00 COMMENT '收入金额',
    expense_amount DECIMAL(10, 2) DEFAULT 0.00 COMMENT '支出金额',
    transaction_type VARCHAR(20) NOT NULL COMMENT '交易类型：INCOME-收入，EXPENSE-支出',
    transaction_desc VARCHAR(500) COMMENT '交易描述',
    payment_channel VARCHAR(50) COMMENT '支付渠道',
    category VARCHAR(50) NOT NULL COMMENT '分类',
    sub_category VARCHAR(50) COMMENT '二级分类',
    manual_remark VARCHAR(500) COMMENT '手工备注',
    include_in_statistics VARCHAR(1) NOT NULL DEFAULT '1' COMMENT '是否计入收支统计：0-不计入，1-计入',
    manual_entry VARCHAR(1) NOT NULL DEFAULT '0' COMMENT '是否手工记账：0-否，1-是',
    data_hash VARCHAR(64) COMMENT '数据指纹（用于重复检测）',
    creator_code VARCHAR(50) COMMENT '创建人编码',
    updater_code VARCHAR(50) COMMENT '更新人编码',
    creator_name VARCHAR(50) COMMENT '创建人姓名',
    updater_name VARCHAR(50) COMMENT '更新人姓名',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted VARCHAR(1) DEFAULT '0' COMMENT '逻辑删除标识：0-未删除，1-已删除',
    INDEX idx_transaction_date (transaction_date),
    INDEX idx_transaction_type (transaction_type),
    INDEX idx_category (category),
    INDEX idx_data_hash (data_hash),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='账单表';

-- ============================================
-- 4. bill_tag（标签表）
-- ============================================
CREATE TABLE IF NOT EXISTS bill_tag (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    tag_name VARCHAR(50) NOT NULL COMMENT '标签名称',
    tag_category VARCHAR(50) COMMENT '标签分类',
    tag_color VARCHAR(20) COMMENT '标签颜色',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序序号',
    creator_code VARCHAR(50) COMMENT '创建人编码',
    updater_code VARCHAR(50) COMMENT '更新人编码',
    creator_name VARCHAR(50) COMMENT '创建人姓名',
    updater_name VARCHAR(50) COMMENT '更新人姓名',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted VARCHAR(1) DEFAULT '0' COMMENT '逻辑删除标识：0-未删除，1-已删除',
    UNIQUE KEY uk_tag_name (tag_name),
    INDEX idx_sort_order (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='标签表';

-- ============================================
-- 5. bill_tag_relation（账单标签关联表）
-- ============================================
CREATE TABLE IF NOT EXISTS bill_tag_relation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    bill_id BIGINT NOT NULL COMMENT '账单ID',
    tag_id BIGINT NOT NULL COMMENT '标签ID',
    creator_code VARCHAR(50) COMMENT '创建人编码',
    updater_code VARCHAR(50) COMMENT '更新人编码',
    creator_name VARCHAR(50) COMMENT '创建人姓名',
    updater_name VARCHAR(50) COMMENT '更新人姓名',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted VARCHAR(1) DEFAULT '0' COMMENT '逻辑删除标识：0-未删除，1-已删除',
    UNIQUE KEY uk_bill_tag (bill_id, tag_id),
    INDEX idx_bill_id (bill_id),
    INDEX idx_tag_id (tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='账单标签关联表';

-- ============================================
-- 6. bill_category（分类表）
-- ============================================
CREATE TABLE IF NOT EXISTS bill_category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    category_name VARCHAR(50) NOT NULL COMMENT '分类名称',
    category_type VARCHAR(20) NOT NULL COMMENT '分类类型：INCOME-收入分类，EXPENSE-支出分类',
    parent_id BIGINT COMMENT '父分类ID（二级分类使用）',
    icon VARCHAR(50) COMMENT '图标',
    enabled VARCHAR(1) NOT NULL DEFAULT '1' COMMENT '是否启用：0-禁用，1-启用',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序序号',
    creator_code VARCHAR(50) COMMENT '创建人编码',
    updater_code VARCHAR(50) COMMENT '更新人编码',
    creator_name VARCHAR(50) COMMENT '创建人姓名',
    updater_name VARCHAR(50) COMMENT '更新人姓名',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted VARCHAR(1) DEFAULT '0' COMMENT '逻辑删除标识：0-未删除，1-已删除',
    INDEX idx_category_type (category_type),
    INDEX idx_parent_id (parent_id),
    INDEX idx_enabled (enabled),
    INDEX idx_sort_order (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分类表';

-- ============================================
-- 7. bill_payment_channel（支付渠道表）
-- ============================================
CREATE TABLE IF NOT EXISTS bill_payment_channel (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    channel_name VARCHAR(50) NOT NULL COMMENT '渠道名称',
    channel_type VARCHAR(20) COMMENT '渠道类型：CASH-现金，BANK_CARD-银行卡，CREDIT_CARD-信用卡，E_WALLET-电子钱包，OTHER-其他',
    icon VARCHAR(50) COMMENT '图标',
    enabled VARCHAR(1) NOT NULL DEFAULT '1' COMMENT '是否启用：0-禁用，1-启用',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序序号',
    creator_code VARCHAR(50) COMMENT '创建人编码',
    updater_code VARCHAR(50) COMMENT '更新人编码',
    creator_name VARCHAR(50) COMMENT '创建人姓名',
    updater_name VARCHAR(50) COMMENT '更新人姓名',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted VARCHAR(1) DEFAULT '0' COMMENT '逻辑删除标识：0-未删除，1-已删除',
    UNIQUE KEY uk_channel_name (channel_name),
    INDEX idx_channel_type (channel_type),
    INDEX idx_enabled (enabled),
    INDEX idx_sort_order (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付渠道表';

-- ============================================
-- 8. bill_data_clean_rule（数据清洗规则表）
-- ============================================
CREATE TABLE IF NOT EXISTS bill_data_clean_rule (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    rule_type VARCHAR(50) NOT NULL COMMENT '规则类型：PAYMENT_CHANNEL-支付渠道，CATEGORY-分类，TRANSACTION_DESC-备注',
    match_fields VARCHAR(500) NOT NULL COMMENT '匹配字段（JSON格式）',
    target_value VARCHAR(200) NOT NULL COMMENT '目标值',
    priority INT NOT NULL DEFAULT 0 COMMENT '优先级（数字越大优先级越高）',
    enabled VARCHAR(1) NOT NULL DEFAULT '1' COMMENT '是否启用：0-禁用，1-启用',
    remark VARCHAR(500) COMMENT '备注说明',
    creator_code VARCHAR(50) COMMENT '创建人编码',
    updater_code VARCHAR(50) COMMENT '更新人编码',
    creator_name VARCHAR(50) COMMENT '创建人姓名',
    updater_name VARCHAR(50) COMMENT '更新人姓名',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted VARCHAR(1) DEFAULT '0' COMMENT '逻辑删除标识：0-未删除，1-已删除',
    INDEX idx_rule_type (rule_type),
    INDEX idx_enabled (enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据清洗规则表';
