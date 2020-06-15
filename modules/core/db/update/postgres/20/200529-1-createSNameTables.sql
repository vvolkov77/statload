create table S_NAME_TABLES (
    ID bigint,
    --
    "NAME_TABLE" varchar(20) not null,
    REMARK varchar(256) not null,
    "TABLE_OWNER" varchar(1),
    "TYPE" varchar(2),
    --
    primary key (ID)
);