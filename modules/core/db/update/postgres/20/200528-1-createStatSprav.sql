create table STATLOAD_STAT_SPRAV (
    ID uuid,
    VERSION integer not null,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    --
    NAME_TABLE varchar(20) not null,
    ID_TABLE bigint,
    --
    primary key (ID)
);