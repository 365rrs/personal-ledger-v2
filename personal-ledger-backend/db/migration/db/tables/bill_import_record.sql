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
    on personal_ledger_v2.bill_import_record (create_time);

create index idx_start_time
    on personal_ledger_v2.bill_import_record (start_time);

create index idx_status
    on personal_ledger_v2.bill_import_record (status);

