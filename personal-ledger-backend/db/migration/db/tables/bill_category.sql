create table bill_category
(
    id            bigint auto_increment comment '主键ID'
        primary key,
    category_name varchar(50)                          not null comment '分类名称',
    category_type varchar(20)                          not null comment '分类类型：INCOME-收入分类，EXPENSE-支出分类',
    parent_id     bigint                               null comment '父分类ID（二级分类使用）',
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
    on personal_ledger_v2.bill_category (category_type);

create index idx_enabled
    on personal_ledger_v2.bill_category (enabled);

create index idx_parent_id
    on personal_ledger_v2.bill_category (parent_id);

create index idx_sort_order
    on personal_ledger_v2.bill_category (sort_order);

