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
    on personal_ledger_v2.bill_tag (sort_order);

