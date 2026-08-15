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
    on personal_ledger_v2.bill_import_detail (convert_status);

create index idx_data_hash
    on personal_ledger_v2.bill_import_detail (data_hash);

create index idx_duplicate_status
    on personal_ledger_v2.bill_import_detail (duplicate_status);

create index idx_import_record_id
    on personal_ledger_v2.bill_import_detail (import_record_id);

create index idx_import_status
    on personal_ledger_v2.bill_import_detail (import_status);

create index idx_ledger_id
    on personal_ledger_v2.bill_import_detail (ledger_id);

