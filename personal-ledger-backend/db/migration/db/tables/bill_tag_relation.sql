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
    on personal_ledger_v2.bill_tag_relation (bill_id);

create index idx_tag_id
    on personal_ledger_v2.bill_tag_relation (tag_id);

