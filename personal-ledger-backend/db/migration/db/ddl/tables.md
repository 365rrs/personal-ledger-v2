# 数据库表设计文档

## 用途
个人账本系统数据库表结构设计文档

## 作者
personal-ledger

## 日期
2025-01-13

---

## 表清单

| 表名 | 说明 | 状态 |
|------|------|------|
| bill | 账单表 | 待创建 |
| bill_category | 分类表 | 待创建 |
| bill_payment_channel | 支付渠道表 | 待创建 |
| bill_data_clean_rule | 数据清洗规则表 | 待创建 |
| bill_budget | 预算表 | 待创建 |
| bill_tag | 标签表 | 待创建 |
| bill_tag_relation | 账单标签关联表 | 待创建 |
| bill_import_record | 导入记录表 | 待创建 |
| bill_import_detail | 导入明细表 | 待创建 |

---

## 1. bill_import_record（导入记录表）

### 表说明
用于记录每次批量导入的文件信息、导入状态和统计数据

### 字段定义

| 字段名 | 类型 | 长度 | 允许NULL | 默认值 | 说明 |
|--------|------|------|----------|--------|------|
| id | BIGINT | - | NO | AUTO_INCREMENT | 主键ID |
| file_name | VARCHAR | 200 | NO | - | 导入文件名 |
| file_size | BIGINT | - | YES | NULL | 文件大小（字节） |
| total_count | INT | - | NO | 0 | 总记录数 |
| processed_count | INT | - | NO | 0 | 已处理记录数 |
| success_count | INT | - | NO | 0 | 成功导入数 |
| fail_count | INT | - | NO | 0 | 失败记录数 |
| status | VARCHAR | 20 | NO | - | 导入状态：PROCESSING-处理中，SUCCESS-成功，FAILED-失败，PARTIAL-部分成功 |
| error_message | TEXT | - | YES | NULL | 错误信息 |
| file_path | VARCHAR | 500 | YES | NULL | 文件存储路径 |
| start_time | DATETIME | - | NO | - | 开始时间 |
| end_time | DATETIME | - | YES | NULL | 结束时间 |
| creator_code | VARCHAR | 50 | YES | NULL | 创建人编码 |
| updater_code | VARCHAR | 50 | YES | NULL | 更新人编码 |
| creator_name | VARCHAR | 50 | YES | NULL | 创建人姓名 |
| updater_name | VARCHAR | 50 | YES | NULL | 更新人姓名 |
| create_time | DATETIME | - | NO | CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | - | NO | CURRENT_TIMESTAMP | 更新时间 |
| deleted | VARCHAR | 1 | NO | '0' | 逻辑删除标识：0-未删除，1-已删除 |

### 索引定义

| 索引名 | 索引类型 | 字段 | 说明 |
|--------|----------|------|------|
| PRIMARY | 主键索引 | id | 主键 |
| idx_status | 普通索引 | status | 状态查询 |
| idx_start_time | 普通索引 | start_time | 时间查询 |
| idx_create_time | 普通索引 | create_time | 创建时间查询 |

### 建表SQL

```sql
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
```

---

## 2. bill_import_detail（导入明细表）

### 表说明
保存导入文件的原始记录数据，用于追溯和问题排查

### 字段定义

| 字段名 | 类型 | 长度 | 允许NULL | 默认值 | 说明 |
|--------|------|------|----------|--------|------|
| id | BIGINT | - | NO | AUTO_INCREMENT | 主键 ID |
| import_record_id | BIGINT | - | NO | - | 导入记录 ID |
| original_data | TEXT | - | YES | NULL | 原始数据（JSON 格式） |
| amount_type | VARCHAR | 20 | YES | NULL | 金额类型（INCOME/EXPENSE） |
| amount | DECIMAL | 10,2 | YES | NULL | 金额 |
| transaction_type | VARCHAR | 100 | YES | NULL | 交易类型（原始值） |
| description | VARCHAR | 500 | YES | NULL | 描述 |
| transaction_time | DATETIME | - | YES | NULL | 交易时间 |
| import_status | VARCHAR | 20 | NO | - | 导入状态：SUCCESS-成功，FAILED-失败 |
| duplicate_status | VARCHAR | 20 | NO | 'UNCHECKED' | 重复状态：UNCHECKED-未检查，UNIQUE-唯一，DUPLICATE-重复 |
| duplicate_ledger_id | BIGINT | - | YES | NULL | 重复的账单ID（如果是重复记录） |
| data_hash | VARCHAR | 64 | YES | NULL | 数据指纹（用于重复检测） |
| convert_status | VARCHAR | 20 | NO | 'PENDING' | 转账单状态：PENDING-待转换，CONVERTED-已转换，SKIPPED-已跳过，DUPLICATE-重复跳过，CONVERT_FAILED-转换失败 |
| convert_error_message | VARCHAR | 500 | YES | NULL | 转换错误信息 |
| error_message | VARCHAR | 500 | YES | NULL | 错误信息 |
| ledger_id | BIGINT | - | YES | NULL | 关联的账单ID（转换成功后） |
| creator_code | VARCHAR | 50 | YES | NULL | 创建人编码 |
| updater_code | VARCHAR | 50 | YES | NULL | 更新人编码 |
| creator_name | VARCHAR | 50 | YES | NULL | 创建人姓名 |
| updater_name | VARCHAR | 50 | YES | NULL | 更新人姓名 |
| create_time | DATETIME | - | NO | CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | - | NO | CURRENT_TIMESTAMP | 更新时间 |
| deleted | VARCHAR | 1 | NO | '0' | 逻辑删除标识：0-未删除，1-已删除 |

### 索引定义

| 索引名 | 索引类型 | 字段 | 说明 |
|--------|----------|------|------|
| PRIMARY | 主键索引 | id | 主键 |
| idx_import_record_id | 普通索引 | import_record_id | 导入记录查询 |
| idx_import_status | 普通索引 | import_status | 导入状态查询 |
| idx_duplicate_status | 普通索引 | duplicate_status | 重复状态查询 |
| idx_data_hash | 普通索引 | data_hash | 数据指纹查询 |
| idx_convert_status | 普通索引 | convert_status | 转换状态查询 |
| idx_ledger_id | 普通索引 | ledger_id | 账单关联查询 |

### 建表SQL

```sql
CREATE TABLE IF NOT EXISTS bill_import_detail (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键 ID',
    import_record_id BIGINT NOT NULL COMMENT '导入记录 ID',
    original_data TEXT COMMENT '原始数据（JSON 格式）',
    amount_type VARCHAR(20) COMMENT '金额类型（INCOME/EXPENSE）',
    amount DECIMAL(10, 2) COMMENT '金额',
    transaction_type VARCHAR(100) COMMENT '交易类型（原始值）',
    description VARCHAR(500) COMMENT '描述',
    transaction_time DATETIME COMMENT '交易时间',
    import_status VARCHAR(20) NOT NULL COMMENT '导入状态：SUCCESS-成功，FAILED-失败',
    duplicate_status VARCHAR(20) NOT NULL DEFAULT 'UNCHECKED' COMMENT '重复状态：UNCHECKED-未检查，UNIQUE-唯一，DUPLICATE-重复',
    duplicate_ledger_id BIGINT COMMENT '重复的账单 ID（如果是重复记录）',
    data_hash VARCHAR(64) COMMENT '数据指纹（用于重复检测）',
    convert_status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '转账单状态：PENDING-待转换，CONVERTED-已转换，SKIPPED-已跳过，DUPLICATE-重复跳过，CONVERT_FAILED-转换失败',
    convert_error_message VARCHAR(500) COMMENT '转换错误信息',
    error_message VARCHAR(500) COMMENT '错误信息',
    ledger_id BIGINT COMMENT '关联的账单 ID（转换成功后）',
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
```

### 表关系

- bill_import_detail.import_record_id → bill_import_record.id（多对一）
- bill_import_detail.ledger_id → bill.id（一对一，转换成功后关联）

### 业务流程

1. **导入阶段**
   - 文件上传后，解析数据并插入 bill_import_detail
   - 计算 data_hash（基于 type + amount + transaction_time + description）
   - import_status 标记为 SUCCESS/FAILED
   - duplicate_status 默认为 UNCHECKED

2. **重复检测阶段**
   - 根据 data_hash 在已有账单中查找是否存在相同记录
   - 如果找到重复：
     - duplicate_status 更新为 DUPLICATE
     - duplicate_ledger_id 记录重复的账单ID
     - convert_status 自动更新为 DUPLICATE
   - 如果唯一：
     - duplicate_status 更新为 UNIQUE
     - convert_status 保持为 PENDING

3. **转换阶段**
   - 仅转换 duplicate_status 为 UNIQUE 且 convert_status 为 PENDING 的记录
   - 转换成功后更新 convert_status 为 CONVERTED
   - 转换失败后更新 convert_status 为 CONVERT_FAILED，记录 convert_error_message
   - 记录 ledger_id 关联账单

4. **跳过记录**
   - 用户可选择跳过某些记录
   - 将 convert_status 更新为 SKIPPED

### 重复检测规则

**数据指纹计算**：
```
data_hash = MD5(amount_type + amount + transaction_time + description)
```

**重复判断逻辑**：
- 在 bill 表中查找相同 data_hash 的记录
- 如果存在且未被删除，则标记为重复

---

## 3. bill（账单表）

### 表说明
记录每一笔收入或支出的详细信息

### 字段定义

| 字段名 | 类型 | 长度 | 允许 NULL | 默认值 | 说明 |
|--------|------|------|----------|--------|------|
| id | BIGINT | - | NO | AUTO_INCREMENT | 主键 ID |
| transaction_date | DATE | - | NO | - | 交易日期 |
| transaction_time | TIME | - | YES | NULL | 交易时间 |
| income_amount | DECIMAL | 10,2 | YES | 0.00 | 收入金额 |
| expense_amount | DECIMAL | 10,2 | YES | 0.00 | 支出金额 |
| amount_type | VARCHAR | 20 | NO | 'EXPENSE' | 金额类型：INCOME-收入，EXPENSE-支出 |
| transaction_type | VARCHAR | 100 | YES | NULL | 交易类型（原始值） |
| transaction_desc | VARCHAR | 500 | YES | NULL | 交易描述 |
| payment_channel | VARCHAR | 50 | YES | NULL | 支付渠道 |
| payment_channel_id | BIGINT | - | YES | NULL | 支付渠道 ID |
| category | VARCHAR | 50 | YES | NULL | 分类 |
| category_id | BIGINT | - | YES | NULL | 分类 ID |
| sub_category | VARCHAR | 50 | YES | NULL | 二级分类 |
| sub_category_id | BIGINT | - | YES | NULL | 二级分类 ID |
| manual_remark | VARCHAR | 500 | YES | NULL | 手工备注 |
| include_in_statistics | VARCHAR | 1 | NO | '1' | 是否计入收支统计：0-不计入，1-计入 |
| manual_entry | VARCHAR | 1 | NO | '0' | 是否手工记账：0-否，1-是 |
| data_hash | VARCHAR | 64 | YES | NULL | 数据指纹（用于重复检测） |
| creator_code | VARCHAR | 50 | YES | NULL | 创建人编码 |
| updater_code | VARCHAR | 50 | YES | NULL | 更新人编码 |
| creator_name | VARCHAR | 50 | YES | NULL | 创建人姓名 |
| updater_name | VARCHAR | 50 | YES | NULL | 更新人姓名 |
| create_time | DATETIME | - | NO | CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | - | NO | CURRENT_TIMESTAMP | 更新时间 |
| deleted | VARCHAR | 1 | NO | '0' | 逻辑删除标识：0-未删除，1-已删除 |

### 索引定义

| 索引名 | 索引类型 | 字段 | 说明 |
|--------|----------|------|------|
| PRIMARY | 主键索引 | id | 主键 |
| idx_transaction_date | 普通索引 | transaction_date | 交易日期查询 |
| idx_transaction_type | 普通索引 | transaction_type | 交易类型查询 |
| idx_category_id | 普通索引 | category_id | 分类 ID 查询 |
| idx_sub_category_id | 普通索引 | sub_category_id | 二级分类 ID 查询 |
| idx_data_hash | 普通索引 | data_hash | 数据指纹查询（重复检测） |
| idx_create_time | 普通索引 | create_time | 创建时间查询 |

### 建表SQL

```sql
CREATE TABLE IF NOT EXISTS bill (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键 ID',
    transaction_date DATE NOT NULL COMMENT '交易日期',
    transaction_time TIME COMMENT '交易时间',
    income_amount DECIMAL(10, 2) DEFAULT 0.00 COMMENT '收入金额',
    expense_amount DECIMAL(10, 2) DEFAULT 0.00 COMMENT '支出金额',
    amount_type VARCHAR(20) NOT NULL DEFAULT 'EXPENSE' COMMENT '金额类型：INCOME-收入，EXPENSE-支出',
    transaction_type VARCHAR(100) COMMENT '交易类型（原始值）',
    transaction_desc VARCHAR(500) COMMENT '交易描述',
    payment_channel VARCHAR(50) COMMENT '支付渠道',
    payment_channel_id BIGINT COMMENT '支付渠道 ID',
    category VARCHAR(50) COMMENT '分类',
    category_id BIGINT COMMENT '分类 ID',
    sub_category VARCHAR(50) COMMENT '二级分类',
    sub_category_id BIGINT COMMENT '二级分类 ID',
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
    INDEX idx_category_id (category_id),
    INDEX idx_create_time (create_time),
    INDEX idx_data_hash (data_hash),
    INDEX idx_sub_category_id (sub_category_id),
    INDEX idx_transaction_date (transaction_date),
    INDEX idx_transaction_type (transaction_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='账单表';
```

### 字段说明

**金额字段设计**：
- income_amount：收入金额，仅收入类型时有值
- expense_amount：支出金额，仅支出类型时有值
- 两个字段分开存储，便于统计和查询

**时间字段设计**：
- transaction_date：交易日期（必填），用于按日期统计
- transaction_time：交易时间（可选），记录具体时间点

**分类字段设计**：
- category：一级分类（可选），如：餐饮、交通、工资等
- category_id：一级分类 ID（可选），关联 bill_category 表
- sub_category：二级分类（可选），如：早餐、午餐、晚餐等
- sub_category_id：二级分类 ID（可选），关联 bill_category 表

**标识字段**：
- include_in_statistics：是否计入收支统计，用于排除某些特殊交易
- manual_entry：是否手工记账，区分手工录入和导入数据
- data_hash：数据指纹，用于重复检测

---

## 4. bill_tag（标签表）

### 表说明
管理账单标签，用于给账单打标签分类

### 字段定义

| 字段名 | 类型 | 长度 | 允许NULL | 默认值 | 说明 |
|--------|------|------|----------|--------|------|
| id | BIGINT | - | NO | AUTO_INCREMENT | 主键 ID |
| tag_name | VARCHAR | 50 | NO | - | 标签名称 |
| tag_category | VARCHAR | 50 | YES | NULL | 标签分类 |
| tag_color | VARCHAR | 20 | YES | NULL | 标签颜色 |
| sort_order | INT | - | NO | 0 | 排序序号 |
| tag_status | VARCHAR | 10 | YES | 'enable' | 状态：enable-启用，disable-停用 |
| creator_code | VARCHAR | 50 | YES | NULL | 创建人编码 |
| updater_code | VARCHAR | 50 | YES | NULL | 更新人编码 |
| creator_name | VARCHAR | 50 | YES | NULL | 创建人姓名 |
| updater_name | VARCHAR | 50 | YES | NULL | 更新人姓名 |
| create_time | DATETIME | - | NO | CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | - | NO | CURRENT_TIMESTAMP | 更新时间 |
| deleted | VARCHAR | 1 | NO | '0' | 逻辑删除标识：0-未删除，1-已删除 |

### 索引定义

| 索引名 | 索引类型 | 字段 | 说明 |
|--------|----------|------|------|
| PRIMARY | 主键索引 | id | 主键 |
| uk_tag_name | 唯一索引 | tag_name | 标签名称唯一 |
| idx_sort_order | 普通索引 | sort_order | 排序查询 |

### 建表SQL

```sql
CREATE TABLE IF NOT EXISTS bill_tag (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键 ID',
    tag_name VARCHAR(50) NOT NULL COMMENT '标签名称',
    tag_category VARCHAR(50) COMMENT '标签分类',
    tag_color VARCHAR(20) COMMENT '标签颜色',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序序号',
    tag_status VARCHAR(10) DEFAULT 'enable' COMMENT '状态：enable-启用，disable-停用',
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
```

---

## 5. bill_tag_relation（账单标签关联表）

### 表说明
账单与标签的多对多关联关系表

### 字段定义

| 字段名 | 类型 | 长度 | 允许NULL | 默认值 | 说明 |
|--------|------|------|----------|--------|------|
| id | BIGINT | - | NO | AUTO_INCREMENT | 主键ID |
| bill_id | BIGINT | - | NO | - | 账单ID |
| tag_id | BIGINT | - | NO | - | 标签ID |
| creator_code | VARCHAR | 50 | YES | NULL | 创建人编码 |
| updater_code | VARCHAR | 50 | YES | NULL | 更新人编码 |
| creator_name | VARCHAR | 50 | YES | NULL | 创建人姓名 |
| updater_name | VARCHAR | 50 | YES | NULL | 更新人姓名 |
| create_time | DATETIME | - | NO | CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | - | NO | CURRENT_TIMESTAMP | 更新时间 |
| deleted | VARCHAR | 1 | NO | '0' | 逻辑删除标识：0-未删除，1-已删除 |

### 索引定义

| 索引名 | 索引类型 | 字段 | 说明 |
|--------|----------|------|------|
| PRIMARY | 主键索引 | id | 主键 |
| uk_bill_tag | 唯一索引 | bill_id, tag_id | 账单和标签组合唯一 |
| idx_bill_id | 普通索引 | bill_id | 账单查询 |
| idx_tag_id | 普通索引 | tag_id | 标签查询 |

### 建表SQL

```sql
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
```

### 表关系

- bill_tag_relation.bill_id → bill.id（多对一）
- bill_tag_relation.tag_id → bill_tag.id（多对一）
- bill (1) ←→ (N) bill_tag_relation ←→ (N) bill_tag（多对多）

### 使用场景

**查询账单的所有标签**：
```sql
SELECT t.* FROM bill_tag t
INNER JOIN bill_tag_relation r ON t.id = r.tag_id
WHERE r.bill_id = ? AND r.deleted = '0' AND t.deleted = '0'
```

**查询某标签下的所有账单**：
```sql
SELECT b.* FROM bill b
INNER JOIN bill_tag_relation r ON b.id = r.bill_id
WHERE r.tag_id = ? AND r.deleted = '0' AND b.deleted = '0'
```

**给账单添加标签**：
```sql
INSERT INTO bill_tag_relation (bill_id, tag_id) VALUES (?, ?)
```

**移除账单标签**：
```sql
UPDATE bill_tag_relation SET deleted = '1' 
WHERE bill_id = ? AND tag_id = ?
```

---

## 6. bill_category（分类表）

### 表说明
管理账单的收入和支出分类

### 字段定义

| 字段名 | 类型 | 长度 | 允许NULL | 默认值 | 说明 |
|--------|------|------|----------|--------|------|
| id | BIGINT | - | NO | AUTO_INCREMENT | 主键ID |
| category_name | VARCHAR | 50 | NO | - | 分类名称 |
| category_type | VARCHAR | 20 | NO | - | 分类类型：INCOME-收入分类，EXPENSE-支出分类 |
| parent_id | BIGINT | - | YES | NULL | 父分类ID（二级分类使用） |
| icon | VARCHAR | 50 | YES | NULL | 图标 |
| enabled | VARCHAR | 1 | NO | '1' | 是否启用：0-禁用，1-启用 |
| sort_order | INT | - | NO | 0 | 排序序号 |
| creator_code | VARCHAR | 50 | YES | NULL | 创建人编码 |
| updater_code | VARCHAR | 50 | YES | NULL | 更新人编码 |
| creator_name | VARCHAR | 50 | YES | NULL | 创建人姓名 |
| updater_name | VARCHAR | 50 | YES | NULL | 更新人姓名 |
| create_time | DATETIME | - | NO | CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | - | NO | CURRENT_TIMESTAMP | 更新时间 |
| deleted | VARCHAR | 1 | NO | '0' | 逻辑删除标识：0-未删除，1-已删除 |

### 索引定义

| 索引名 | 索引类型 | 字段 | 说明 |
|--------|----------|------|------|
| PRIMARY | 主键索引 | id | 主键 |
| idx_category_type | 普通索引 | category_type | 分类类型查询 |
| idx_parent_id | 普通索引 | parent_id | 父分类查询 |
| idx_enabled | 普通索引 | enabled | 启用状态查询 |
| idx_sort_order | 普通索引 | sort_order | 排序查询 |

### 建表SQL

```sql
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
```

### 分类结构

**支持两级分类**：
- 一级分类：parent_id 为 NULL
- 二级分类：parent_id 为一级分类的 id

**示例数据**：

```sql
-- 支出分类
INSERT INTO bill_category (category_name, category_type, parent_id, icon, sort_order) VALUES
('餐饮', 'EXPENSE', NULL, 'icon-food', 1),
('早餐', 'EXPENSE', 1, NULL, 1),
('午餐', 'EXPENSE', 1, NULL, 2),
('晚餐', 'EXPENSE', 1, NULL, 3),
('交通', 'EXPENSE', NULL, 'icon-transport', 2),
('公交', 'EXPENSE', 5, NULL, 1),
('地铁', 'EXPENSE', 5, NULL, 2),
('打车', 'EXPENSE', 5, NULL, 3),
('购物', 'EXPENSE', NULL, 'icon-shopping', 3),
('娱乐', 'EXPENSE', NULL, 'icon-entertainment', 4),
('住房', 'EXPENSE', NULL, 'icon-house', 5),
('医疗', 'EXPENSE', NULL, 'icon-medical', 6),
('教育', 'EXPENSE', NULL, 'icon-education', 7),
('其他支出', 'EXPENSE', NULL, 'icon-other', 99);

-- 收入分类
INSERT INTO bill_category (category_name, category_type, parent_id, icon, sort_order) VALUES
('工资', 'INCOME', NULL, 'icon-salary', 1),
('奖金', 'INCOME', NULL, 'icon-bonus', 2),
('投资收益', 'INCOME', NULL, 'icon-investment', 3),
('其他收入', 'INCOME', NULL, 'icon-other', 99);
```

### 使用场景

**查询一级分类**：
```sql
SELECT * FROM bill_category 
WHERE parent_id IS NULL AND category_type = 'EXPENSE' 
  AND enabled = '1' AND deleted = '0'
ORDER BY sort_order;
```

**查询二级分类**：
```sql
SELECT * FROM bill_category 
WHERE parent_id = ? AND enabled = '1' AND deleted = '0'
ORDER BY sort_order;
```

**查询分类树**：
```sql
SELECT 
    c1.id AS parent_id,
    c1.category_name AS parent_name,
    c2.id AS child_id,
    c2.category_name AS child_name
FROM bill_category c1
LEFT JOIN bill_category c2 ON c1.id = c2.parent_id 
  AND c2.enabled = '1' AND c2.deleted = '0'
WHERE c1.parent_id IS NULL AND c1.category_type = 'EXPENSE' 
  AND c1.enabled = '1' AND c1.deleted = '0'
ORDER BY c1.sort_order, c2.sort_order;
```

### 字段说明

**enabled 字段**：
- 用于控制分类是否可用
- 禁用后的分类不在前端显示，但不删除数据
- 已使用该分类的账单仍然保留分类信息
- 可以重新启用

---

## 7. bill_payment_channel（支付渠道表）

### 表说明
管理账单的支付渠道

### 字段定义

| 字段名 | 类型 | 长度 | 允许NULL | 默认值 | 说明 |
|--------|------|------|----------|--------|------|
| id | BIGINT | - | NO | AUTO_INCREMENT | 主键ID |
| channel_name | VARCHAR | 50 | NO | - | 渠道名称 |
| channel_type | VARCHAR | 20 | YES | NULL | 渠道类型：CASH-现金，BANK_CARD-银行卡，CREDIT_CARD-信用卡，E_WALLET-电子钱包，OTHER-其他 |
| icon | VARCHAR | 50 | YES | NULL | 图标 |
| enabled | VARCHAR | 1 | NO | '1' | 是否启用：0-禁用，1-启用 |
| sort_order | INT | - | NO | 0 | 排序序号 |
| creator_code | VARCHAR | 50 | YES | NULL | 创建人编码 |
| updater_code | VARCHAR | 50 | YES | NULL | 更新人编码 |
| creator_name | VARCHAR | 50 | YES | NULL | 创建人姓名 |
| updater_name | VARCHAR | 50 | YES | NULL | 更新人姓名 |
| create_time | DATETIME | - | NO | CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | - | NO | CURRENT_TIMESTAMP | 更新时间 |
| deleted | VARCHAR | 1 | NO | '0' | 逻辑删除标识：0-未删除，1-已删除 |

### 索引定义

| 索引名 | 索引类型 | 字段 | 说明 |
|--------|----------|------|------|
| PRIMARY | 主键索引 | id | 主键 |
| uk_channel_name | 唯一索引 | channel_name | 渠道名称唯一 |
| idx_channel_type | 普通索引 | channel_type | 渠道类型查询 |
| idx_enabled | 普通索引 | enabled | 启用状态查询 |
| idx_sort_order | 普通索引 | sort_order | 排序查询 |

### 建表SQL

```sql
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
```

### 预设数据

```sql
-- 现金类
INSERT INTO bill_payment_channel (channel_name, channel_type, icon, sort_order) VALUES
('现金', 'CASH', 'icon-cash', 1);

-- 银行卡类
INSERT INTO bill_payment_channel (channel_name, channel_type, icon, sort_order) VALUES
('工商银行', 'BANK_CARD', 'icon-icbc', 2),
('建设银行', 'BANK_CARD', 'icon-ccb', 3),
('农业银行', 'BANK_CARD', 'icon-abc', 4),
('中国银行', 'BANK_CARD', 'icon-boc', 5),
('招商银行', 'BANK_CARD', 'icon-cmb', 6);

-- 信用卡类
INSERT INTO bill_payment_channel (channel_name, channel_type, icon, sort_order) VALUES
('信用卡', 'CREDIT_CARD', 'icon-credit-card', 7);

-- 电子钱包类
INSERT INTO bill_payment_channel (channel_name, channel_type, icon, sort_order) VALUES
('微信支付', 'E_WALLET', 'icon-wechat', 8),
('支付宝', 'E_WALLET', 'icon-alipay', 9),
('云闪付', 'E_WALLET', 'icon-unionpay', 10);

-- 其他
INSERT INTO bill_payment_channel (channel_name, channel_type, icon, sort_order) VALUES
('其他渠道', 'OTHER', 'icon-other', 99);
```

### 使用场景

**查询所有启用的渠道**：
```sql
SELECT * FROM bill_payment_channel 
WHERE enabled = '1' AND deleted = '0'
ORDER BY sort_order;
```

**按类型查询渠道**：
```sql
SELECT * FROM bill_payment_channel 
WHERE channel_type = 'E_WALLET' AND enabled = '1' AND deleted = '0'
ORDER BY sort_order;
```

**查询渠道使用统计**：
```sql
SELECT 
    pc.id,
    pc.channel_name,
    pc.channel_type,
    COUNT(b.id) AS usage_count,
    SUM(CASE WHEN b.transaction_type = 'INCOME' THEN b.income_amount ELSE 0 END) AS total_income,
    SUM(CASE WHEN b.transaction_type = 'EXPENSE' THEN b.expense_amount ELSE 0 END) AS total_expense
FROM bill_payment_channel pc
LEFT JOIN bill b ON pc.channel_name = b.payment_channel 
    AND b.deleted = '0'
    AND b.transaction_date BETWEEN ? AND ?
WHERE pc.enabled = '1' AND pc.deleted = '0'
GROUP BY pc.id, pc.channel_name, pc.channel_type
ORDER BY usage_count DESC;
```

### 字段说明

**channel_type 字段**：
- 用于对渠道进行分类
- 可选字段，用户可以不选择类型
- 前端可以按类型分组展示渠道

**enabled 字段**：
- 用于控制渠道是否可用
- 禁用后的渠道不在前端显示，但不删除数据
- 已使用该渠道的账单仍然保留渠道信息
- 可以重新启用

### 与账单表的关系

- bill.payment_channel 字段存储渠道名称（VARCHAR）
- 通过渠道名称关联 bill_payment_channel.channel_name
- 这种设计允许：
  - 账单记录保留历史渠道信息（即使渠道被删除）
  - 灵活修改渠道名称而不影响历史数据
  - 支持用户自由输入渠道名称（不强制选择预设渠道）

---

## 8. bill_data_clean_rule（数据清洗规则表）

### 表说明
配置数据清洗的映射规则，用于账单导入时的数据清洗。支持多字段组合匹配。

### 字段定义

| 字段名 | 类型 | 长度 | 允许NULL | 默认值 | 说明 |
|--------|------|------|----------|--------|------|
| id | BIGINT | - | NO | AUTO_INCREMENT | 主键ID |
| rule_type | VARCHAR | 50 | NO | - | 规则类型：PAYMENT_CHANNEL-支付渠道，CATEGORY-分类，TRANSACTION_DESC-备注 |
| match_fields | VARCHAR | 500 | NO | - | 匹配字段（JSON格式） |
| target_value | VARCHAR | 200 | NO | - | 目标值 |
| priority | INT | - | NO | 0 | 优先级（数字越大优先级越高） |
| enabled | VARCHAR | 1 | NO | '1' | 是否启用：0-禁用，1-启用 |
| remark | VARCHAR | 500 | YES | NULL | 备注说明 |
| creator_code | VARCHAR | 50 | YES | NULL | 创建人编码 |
| updater_code | VARCHAR | 50 | YES | NULL | 更新人编码 |
| creator_name | VARCHAR | 50 | YES | NULL | 创建人姓名 |
| updater_name | VARCHAR | 50 | YES | NULL | 更新人姓名 |
| create_time | DATETIME | - | NO | CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | - | NO | CURRENT_TIMESTAMP | 更新时间 |
| deleted | VARCHAR | 1 | NO | '0' | 逻辑删除标识：0-未删除，1-已删除 |

### 索引定义

| 索引名 | 索引类型 | 字段 | 说明 |
|--------|----------|------|------|
| PRIMARY | 主键索引 | id | 主键 |
| idx_rule_type | 普通索引 | rule_type | 规则类型查询 |
| idx_enabled | 普通索引 | enabled | 启用状态查询 |

### 建表SQL

```sql
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
```

### match_fields 格式

JSON 格式，支持多字段匹配：

**支付渠道清洗**：
```json
{
  "payment_channel": "微信"
}
```

**分类清洗**：
```json
{
  "category": "吃饭",
  "transaction_type": "EXPENSE"
}
```

**备注清洗**：
```json
{
  "transaction_desc": "美团外卖"
}
```

### 预设数据

```sql
-- 支付渠道映射
INSERT INTO bill_data_clean_rule (rule_type, match_fields, target_value, priority, remark) VALUES
('PAYMENT_CHANNEL', '{"payment_channel":"微信"}', '微信支付', 100, '微信'),
('PAYMENT_CHANNEL', '{"payment_channel":"支付宝"}', '支付宝', 100, '支付宝'),
('PAYMENT_CHANNEL', '{"payment_channel":"工行"}', '工商银行', 100, '工商银行'),
('PAYMENT_CHANNEL', '{"payment_channel":"建行"}', '建设银行', 100, '建设银行'),
('PAYMENT_CHANNEL', '{"payment_channel":"现金"}', '现金', 100, '现金');

-- 分类映射
INSERT INTO bill_data_clean_rule (rule_type, match_fields, target_value, priority, remark) VALUES
('CATEGORY', '{"category":"吃饭"}', '餐饮', 100, '餐饮'),
('CATEGORY', '{"category":"打车"}', '交通', 100, '交通'),
('CATEGORY', '{"category":"购物"}', '购物', 100, '购物');

-- 默认分类
INSERT INTO bill_data_clean_rule (rule_type, match_fields, target_value, priority, remark) VALUES
('CATEGORY', '{"transaction_type":"EXPENSE"}', '其他支出', 0, '支出默认'),
('CATEGORY', '{"transaction_type":"INCOME"}', '其他收入', 0, '收入默认');

-- 备注映射
INSERT INTO bill_data_clean_rule (rule_type, match_fields, target_value, priority, remark) VALUES
('TRANSACTION_DESC', '{"transaction_desc":"美团外卖"}', '外卖订单', 100, '美团外卖'),
('TRANSACTION_DESC', '{"transaction_desc":"滴滴出行"}', '打车费用', 100, '滴滴出行');
```

### 使用场景

**支付渠道清洗**：
```sql
SELECT target_value FROM bill_data_clean_rule 
WHERE rule_type = 'PAYMENT_CHANNEL' 
  AND JSON_EXTRACT(match_fields, '$.payment_channel') = '微信'
  AND enabled = '1' 
  AND deleted = '0'
ORDER BY priority DESC
LIMIT 1;
```

**分类清洗（多字段匹配）**：
```sql
-- 先匹配具体分类
SELECT target_value FROM bill_data_clean_rule 
WHERE rule_type = 'CATEGORY' 
  AND JSON_EXTRACT(match_fields, '$.category') = '吃饭'
  AND JSON_EXTRACT(match_fields, '$.transaction_type') = 'EXPENSE'
  AND enabled = '1' 
  AND deleted = '0'
ORDER BY priority DESC
LIMIT 1;

-- 匹配失败则使用默认分类
SELECT target_value FROM bill_data_clean_rule 
WHERE rule_type = 'CATEGORY' 
  AND JSON_EXTRACT(match_fields, '$.transaction_type') = 'EXPENSE'
  AND JSON_LENGTH(match_fields) = 1
  AND enabled = '1' 
  AND deleted = '0'
ORDER BY priority DESC
LIMIT 1;
```

**备注清洗**：
```sql
SELECT target_value FROM bill_data_clean_rule 
WHERE rule_type = 'TRANSACTION_DESC' 
  AND JSON_EXTRACT(match_fields, '$.transaction_desc') = '美团外卖'
  AND enabled = '1' 
  AND deleted = '0'
ORDER BY priority DESC
LIMIT 1;
```

### 字段说明

**rule_type 字段**：
- PAYMENT_CHANNEL：支付渠道映射规则
- CATEGORY：分类映射规则
- TRANSACTION_DESC：备注映射规则

**match_fields 字段**：
- JSON格式，支持多字段组合匹配
- 所有字段必须同时匹配才返回目标值
- 支持的字段：payment_channel、category、transaction_type、transaction_desc

**priority 字段**：
- 数字越大优先级越高
- 建议：具体规则 100，默认规则 0
- 相同优先级按 id 升序

### 清洗流程

```
1. 交易类型清洗（根据金额判断）
   ↓
2. 分类清洗（根据 category + transaction_type 匹配）
   ↓
3. 支付渠道清洗（根据 payment_channel 匹配）
   ↓
4. 备注清洗（根据 transaction_desc 匹配）
```

---

## 9. bill_budget（预算表）

### 表说明
设置每月各分类的预算额度

### 字段定义

待补充...
