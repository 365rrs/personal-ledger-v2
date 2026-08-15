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
    data_hash             varchar(64)                           not null comment '数据指纹（用于重复检测）',
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
    on personal_ledger_v2.bill (category_id);

create index idx_create_time
    on personal_ledger_v2.bill (create_time);

create index idx_data_hash
    on personal_ledger_v2.bill (data_hash);

create index idx_sub_category_id
    on personal_ledger_v2.bill (sub_category_id);

create index idx_transaction_date
    on personal_ledger_v2.bill (transaction_date);

create index idx_transaction_type
    on personal_ledger_v2.bill (transaction_type);

