create database chat;

create table user
(
    userId            varchar(12) primary key               not null comment '用户id',
    email             varchar(50) default null comment '邮箱',
    nickName          varchar(20) default null comment '昵称',
    joinType          tinyint(1)  default null comment '0:直接加入 1:同意后加好友',
    sex               tinyint(1)  default null comment '性别 0:男 1:女',
    password          varchar(32) default null comment '密码',
    personalSignature varchar(50) default null comment '个性签名',
    status            tinyint(1)  default null comment '状态',
    createTime        datetime    default CURRENT_TIMESTAMP not null comment '创建时间',
    lastLoginTime     datetime    default null comment '最后登录时间',
    areaName          varchar(50) default null comment '地区',
    areaCode          varchar(50) default null comment '地区编号',
    lastOffTime       bigint(13)  default null comment '最后离开时间',
    isDelete          tinyint(1)  default 0                 not null comment '是否删除',
    unique key idx_key_email (email)
) comment '用户表';

create table user_beauty
(
    id     int(11) primary key not null comment 'id',
    email  varchar(50)         not null comment '邮箱',
    userId varchar(12)         not null comment '用户id',
    status tinyint(1) comment '0:未使用 1:已使用',
    unique key idx_key_user_id (userId),
    unique key idx_key_email (email)
) comment '靓号表';
