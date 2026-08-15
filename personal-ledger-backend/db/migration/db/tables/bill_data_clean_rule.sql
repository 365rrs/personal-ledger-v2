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
    on personal_ledger_v2.bill_data_clean_rule (enabled);

create index idx_rule_type
    on personal_ledger_v2.bill_data_clean_rule (rule_type);

