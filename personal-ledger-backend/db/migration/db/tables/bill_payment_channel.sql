create table bill_payment_channel
(
    id           bigint auto_increment comment '主键ID'
        primary key,
    channel_name varchar(50)                          not null comment '渠道名称',
    channel_type varchar(20)                          null comment '渠道类型：CASH-现金，BANK_CARD-银行卡，CREDIT_CARD-信用卡，E_WALLET-电子钱包，OTHER-其他',
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
    on personal_ledger_v2.bill_payment_channel (channel_type);

create index idx_enabled
    on personal_ledger_v2.bill_payment_channel (enabled);

create index idx_sort_order
    on personal_ledger_v2.bill_payment_channel (sort_order);

