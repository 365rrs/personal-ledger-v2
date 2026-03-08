create table bill
(
    id                    bigint auto_increment comment '主键ID'
        primary key,
    transaction_date      date                                  not null comment '交易日期',
    transaction_time      time                                  null comment '交易时间',
    income_amount         decimal(10, 2)                        null comment '收入金额',
    expense_amount        decimal(10, 2)                        null comment '支出金额',
    amount_type           varchar(20) default 'EXPENSE'         not null comment '金额类型：INCOME-收入，EXPENSE-支出',
    transaction_type      varchar(100)                          null comment '交易类型（原始值）',
    transaction_desc      varchar(500)                          null comment '交易描述',
    payment_channel       varchar(50)                           null comment '支付渠道',
    payment_channel_id    bigint                                null comment '支付渠道 ID',
    category              varchar(50)                           null comment '分类',
    category_id           bigint                                null comment '分类 ID',
    sub_category          varchar(50)                           null comment '二级分类',
    sub_category_id       bigint                                null comment '二级分类 ID',
    manual_remark         varchar(500)                          null comment '手工备注',
    include_in_statistics varchar(1)  default '1'               not null comment '是否计入收支统计：0-不计入，1-计入',
    manual_entry          varchar(1)  default '0'               not null comment '是否手工记账：0-否，1-是',
    data_hash             varchar(64)                           null comment '数据指纹（用于重复检测）',
    creator_code          varchar(50)                           null comment '创建人编码',
    updater_code          varchar(50)                           null comment '更新人编码',
    creator_name          varchar(50)                           null comment '创建人姓名',
    updater_name          varchar(50)                           null comment '更新人姓名',
    create_time           datetime    default CURRENT_TIMESTAMP null comment '创建时间',
    update_time           datetime    default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    deleted               varchar(1)  default '0'               null comment '逻辑删除标识：0-未删除，1-已删除'
)
    comment '账单表';

create index idx_category_id
    on bill (category_id);

create index idx_create_time
    on bill (create_time);

create index idx_data_hash
    on bill (data_hash);

create index idx_sub_category_id
    on bill (sub_category_id);

create index idx_transaction_date
    on bill (transaction_date);

create index idx_transaction_type
    on bill (transaction_type);

create table bill_category
(
    id            bigint auto_increment comment '主键ID'
        primary key,
    category_name varchar(50)                          not null comment '分类名称',
    category_type varchar(20)                          not null comment '分类类型：INCOME-收入分类，EXPENSE-支出分类',
    parent_id     bigint                               null comment '父分类ID（二级分类使用）',
    icon          varchar(50)                          null comment '图标',
    enabled       varchar(1) default '1'               not null comment '是否启用：0-禁用，1-启用',
    sort_order    int        default 0                 not null comment '排序序号',
    creator_code  varchar(50)                          null comment '创建人编码',
    updater_code  varchar(50)                          null comment '更新人编码',
    creator_name  varchar(50)                          null comment '创建人姓名',
    updater_name  varchar(50)                          null comment '更新人姓名',
    create_time   datetime   default CURRENT_TIMESTAMP null comment '创建时间',
    update_time   datetime   default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    deleted       varchar(1) default '0'               null comment '逻辑删除标识：0-未删除，1-已删除'
)
    comment '分类表';

create index idx_category_type
    on bill_category (category_type);

create index idx_enabled
    on bill_category (enabled);

create index idx_parent_id
    on bill_category (parent_id);

create index idx_sort_order
    on bill_category (sort_order);

create table bill_data_clean_rule
(
    id           bigint auto_increment comment '主键ID'
        primary key,
    rule_type    varchar(50)                          not null comment '规则类型：PAYMENT_CHANNEL-支付渠道，CATEGORY-分类，TRANSACTION_DESC-备注',
    match_fields varchar(500)                         not null comment '匹配字段（JSON格式）',
    target_value varchar(200)                         not null comment '目标值',
    priority     int        default 0                 not null comment '优先级（数字越大优先级越高）',
    enabled      varchar(1) default '1'               not null comment '是否启用：0-禁用，1-启用',
    remark       varchar(500)                         null comment '备注说明',
    creator_code varchar(50)                          null comment '创建人编码',
    updater_code varchar(50)                          null comment '更新人编码',
    creator_name varchar(50)                          null comment '创建人姓名',
    updater_name varchar(50)                          null comment '更新人姓名',
    create_time  datetime   default CURRENT_TIMESTAMP null comment '创建时间',
    update_time  datetime   default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    deleted      varchar(1) default '0'               null comment '逻辑删除标识：0-未删除，1-已删除'
)
    comment '数据清洗规则表';

create index idx_enabled
    on bill_data_clean_rule (enabled);

create index idx_rule_type
    on bill_data_clean_rule (rule_type);

create table bill_import_detail
(
    id                    bigint auto_increment comment '主键ID'
        primary key,
    import_record_id      bigint                                not null comment '导入记录ID',
    original_data         text                                  null comment '原始数据（JSON格式）',
    amount_type           varchar(20)                           null comment '金额类型（INCOME/EXPENSE）',
    amount                decimal(10, 2)                        null comment '金额',
    transaction_type      varchar(100)                          null comment '交易类型（原始值）',
    description           varchar(500)                          null comment '描述',
    transaction_time      datetime                              null comment '交易时间',
    import_status         varchar(20)                           not null comment '导入状态：SUCCESS-成功，FAILED-失败',
    duplicate_status      varchar(20) default 'UNCHECKED'       not null comment '重复状态：UNCHECKED-未检查，UNIQUE-唯一，DUPLICATE-重复',
    duplicate_ledger_id   bigint                                null comment '重复的账单ID（如果是重复记录）',
    data_hash             varchar(64)                           null comment '数据指纹（用于重复检测）',
    convert_status        varchar(20) default 'PENDING'         not null comment '转账单状态：PENDING-待转换，CONVERTED-已转换，SKIPPED-已跳过，DUPLICATE-重复跳过，CONVERT_FAILED-转换失败',
    convert_error_message varchar(500)                          null comment '转换错误信息',
    error_message         varchar(500)                          null comment '错误信息',
    ledger_id             bigint                                null comment '关联的账单ID（转换成功后）',
    creator_code          varchar(50)                           null comment '创建人编码',
    updater_code          varchar(50)                           null comment '更新人编码',
    creator_name          varchar(50)                           null comment '创建人姓名',
    updater_name          varchar(50)                           null comment '更新人姓名',
    create_time           datetime    default CURRENT_TIMESTAMP null comment '创建时间',
    update_time           datetime    default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    deleted               varchar(1)  default '0'               null comment '逻辑删除标识：0-未删除，1-已删除'
)
    comment '导入明细表';

create index idx_convert_status
    on bill_import_detail (convert_status);

create index idx_data_hash
    on bill_import_detail (data_hash);

create index idx_duplicate_status
    on bill_import_detail (duplicate_status);

create index idx_import_record_id
    on bill_import_detail (import_record_id);

create index idx_import_status
    on bill_import_detail (import_status);

create index idx_ledger_id
    on bill_import_detail (ledger_id);

create table bill_import_record
(
    id              bigint auto_increment comment '主键ID'
        primary key,
    file_name       varchar(200)                         not null comment '导入文件名',
    file_size       bigint                               null comment '文件大小（字节）',
    total_count     int        default 0                 not null comment '总记录数',
    processed_count int        default 0                 not null comment '已处理记录数',
    success_count   int        default 0                 not null comment '成功导入数',
    fail_count      int        default 0                 not null comment '失败记录数',
    status          varchar(20)                          not null comment '导入状态：PROCESSING-处理中，SUCCESS-成功，FAILED-失败，PARTIAL-部分成功',
    error_message   text                                 null comment '错误信息',
    file_path       varchar(500)                         null comment '文件存储路径',
    start_time      datetime                             not null comment '开始时间',
    end_time        datetime                             null comment '结束时间',
    creator_code    varchar(50)                          null comment '创建人编码',
    updater_code    varchar(50)                          null comment '更新人编码',
    creator_name    varchar(50)                          null comment '创建人姓名',
    updater_name    varchar(50)                          null comment '更新人姓名',
    create_time     datetime   default CURRENT_TIMESTAMP null comment '创建时间',
    update_time     datetime   default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    deleted         varchar(1) default '0'               null comment '逻辑删除标识：0-未删除，1-已删除'
)
    comment '导入记录表';

create index idx_create_time
    on bill_import_record (create_time);

create index idx_start_time
    on bill_import_record (start_time);

create index idx_status
    on bill_import_record (status);

create table bill_payment_channel
(
    id           bigint auto_increment comment '主键ID'
        primary key,
    channel_name varchar(50)                          not null comment '渠道名称',
    channel_type varchar(20)                          null comment '渠道类型：CASH-现金，BANK_CARD-银行卡，CREDIT_CARD-信用卡，E_WALLET-电子钱包，OTHER-其他',
    icon         varchar(50)                          null comment '图标',
    enabled      varchar(1) default '1'               not null comment '是否启用：0-禁用，1-启用',
    sort_order   int        default 0                 not null comment '排序序号',
    creator_code varchar(50)                          null comment '创建人编码',
    updater_code varchar(50)                          null comment '更新人编码',
    creator_name varchar(50)                          null comment '创建人姓名',
    updater_name varchar(50)                          null comment '更新人姓名',
    create_time  datetime   default CURRENT_TIMESTAMP null comment '创建时间',
    update_time  datetime   default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    deleted      varchar(1) default '0'               null comment '逻辑删除标识：0-未删除，1-已删除',
    constraint uk_channel_name
        unique (channel_name)
)
    comment '支付渠道表';

create index idx_channel_type
    on bill_payment_channel (channel_type);

create index idx_enabled
    on bill_payment_channel (enabled);

create index idx_sort_order
    on bill_payment_channel (sort_order);

create table bill_tag
(
    id           bigint auto_increment comment '主键ID'
        primary key,
    tag_name     varchar(50)                           not null comment '标签名称',
    tag_category varchar(50)                           null comment '标签分类',
    tag_color    varchar(20)                           null comment '标签颜色',
    sort_order   int         default 0                 not null comment '排序序号',
    tag_status   varchar(10) default 'enable'          null comment '状态：enable-启用，disable-停用',
    creator_code varchar(50)                           null comment '创建人编码',
    updater_code varchar(50)                           null comment '更新人编码',
    creator_name varchar(50)                           null comment '创建人姓名',
    updater_name varchar(50)                           null comment '更新人姓名',
    create_time  datetime    default CURRENT_TIMESTAMP null comment '创建时间',
    update_time  datetime    default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    deleted      varchar(1)  default '0'               null comment '逻辑删除标识：0-未删除，1-已删除',
    constraint uk_tag_name
        unique (tag_name)
)
    comment '标签表';

create index idx_sort_order
    on bill_tag (sort_order);

create table bill_tag_relation
(
    id           bigint auto_increment comment '主键ID'
        primary key,
    bill_id      bigint                               not null comment '账单ID',
    tag_id       bigint                               not null comment '标签ID',
    creator_code varchar(50)                          null comment '创建人编码',
    updater_code varchar(50)                          null comment '更新人编码',
    creator_name varchar(50)                          null comment '创建人姓名',
    updater_name varchar(50)                          null comment '更新人姓名',
    create_time  datetime   default CURRENT_TIMESTAMP null comment '创建时间',
    update_time  datetime   default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    deleted      varchar(1) default '0'               null comment '逻辑删除标识：0-未删除，1-已删除'
)
    comment '账单标签关联表';

create index idx_bill_id
    on bill_tag_relation (bill_id);

create index idx_tag_id
    on bill_tag_relation (tag_id);

