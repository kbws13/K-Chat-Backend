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

create table group_info
(
    id         varchar(12) primary key not null comment '群组id',
    name       varchar(20)             null default null comment '群组名',
    ownerId    varchar(12)             null default null comment '群主id',
    createTime datetime                     default CURRENT_TIMESTAMP not null comment '创建时间',
    notice     varchar(500)            null default null comment '群公告',
    joinType   tinyint(1)              null default null comment '0:直接加入 1:管理员同意后加入',
    status     tinyint(1)              null default 1 comment '1:正常 0:解散'
) comment '群组表';

create table user_contact
(
    userId      varchar(12) not null comment '用户id',
    contactId   varchar(12) not null comment '联系人id或者群组id',
    contactType tinyint(1)  null default null comment '联系人类型 0:好友 1:群组',
    createTime  datetime         default CURRENT_TIMESTAMP not null comment '创建时间',
    status      tinyint(1)  null default null comment '状态 0:非好友 1:好友 2:已删除 3:拉黑',
    updateTime  datetime         default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    primary key (userId, contactId) using btree,
    index idx_contact_id (contactId) using btree
) comment '联系人表';

create table user_contact_apply
(
    id            int primary key auto_increment comment '自增id',
    applyId       varchar(12)  not null comment '申请人id',
    receiveId     varchar(12)  not null comment '接收人id',
    contactType   tinyint(1)   not null comment '联系人类型 0:好友 1:群组',
    contactId     varchar(12)  null     default null comment '联系人或者群组id',
    lastApplyTime bigint       null     default null comment '最后申请时间',
    status        tinyint(1)   not null default 0 comment '状态 0:待处理 1:已同意 2:已拒绝 3:已拉黑',
    applyInfo     varchar(100) null     default null comment '申请信息',
    unique index idx_key (applyId, receiveId, contactId),
    index idx_last_apply_time (lastApplyTime)
) comment '联系人申请表';

create table app_update
(
    id           int primary key auto_increment comment 'id',
    version      varchar(10)                        null comment '版本号',
    updateDesc   varchar(500)                       null comment '更新信息',
    createTime   datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    status       tinyint(1)                         null comment '0:未发布 1:灰度发布 2:全部发布',
    grayscaleUid varchar(1000)                      null comment '灰度uid',
    fileType     tinyint(1)                         null comment '文件类型 0:本地文件 1:外链',
    outerLink    varchar(200)                       null comment '外链地址',
    unique index idx_key(version)
) comment 'app发布表';
