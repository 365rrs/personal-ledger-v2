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
| bill_budget | 预算表 | 待创建 |
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
| id | BIGINT | - | NO | AUTO_INCREMENT | 主键ID |
| import_record_id | BIGINT | - | NO | - | 导入记录ID |
| row_number | INT | - | NO | - | 行号 |
| original_data | TEXT | - | YES | NULL | 原始数据（JSON格式） |
| type | VARCHAR | 20 | YES | NULL | 类型 |
| amount | DECIMAL | 10,2 | YES | NULL | 金额 |
| category | VARCHAR | 50 | YES | NULL | 分类 |
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
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    import_record_id BIGINT NOT NULL COMMENT '导入记录ID',
    row_number INT NOT NULL COMMENT '行号',
    original_data TEXT COMMENT '原始数据（JSON格式）',
    type VARCHAR(20) COMMENT '类型',
    amount DECIMAL(10, 2) COMMENT '金额',
    category VARCHAR(50) COMMENT '分类',
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
data_hash = MD5(type + amount + transaction_time + description)
```

**重复判断逻辑**：
- 在 bill 表中查找相同 data_hash 的记录
- 如果存在且未被删除，则标记为重复

---

## 3. bill（账单表）

### 表说明
记录每一笔收入或支出的详细信息

### 字段定义

待补充...

---

## 4. bill_category（分类表）

### 表说明
管理收入和支出的分类

### 字段定义

待补充...

---

## 5. bill_budget（预算表）

### 表说明
设置每月各分类的预算额度

### 字段定义

待补充...
