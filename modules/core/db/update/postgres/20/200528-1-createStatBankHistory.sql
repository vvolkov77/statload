create table STATLOAD_STAT_BANK_HISTORY (
    ID uuid,
    VERSION integer not null,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    --
    NAME varchar(256) not null,
    ID_FILIAL bigint not null,
    REGION bigint not null,
    --
    primary key (ID)
);