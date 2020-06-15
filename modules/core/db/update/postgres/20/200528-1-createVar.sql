create table STATLOAD_VAR (
    ID uuid,
    VERSION integer not null,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    --
    ID_REPORT uuid not null,
    CODE varchar(255) not null,
    NROW integer,
    NCOL integer,
    ID_STATPOKAZ uuid,
    ID_SPRAV uuid,
    STAT_KEY_FIELD varchar(255),
    STAT_RES_FIELD varchar(255),
    --
    primary key (ID)
);