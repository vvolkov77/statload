create table STATLOAD_STAT_FORM (
    ID uuid,
    VERSION integer not null,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    --
    ID_FORM bigint,
    SHORT_NAME varchar(255),
    LONG_NAME varchar(512),
    --
    primary key (ID)
);