-- begin STATLOAD_REPORT
create table STATLOAD_REPORT (
    ID uuid,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    --
    NAME_SHORT varchar(50),
    NAME_LONG text,
    VID integer not null,
    ID_STAT uuid,
    STAT_SCHEMA varchar(50),
    --
    primary key (ID)
)^
-- end STATLOAD_REPORT
-- begin STATLOAD_STAT_BANK_HISTORY
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
    MFO varchar(255),
    ID_FILIAL bigint not null,
    REGION bigint not null,
    --
    primary key (ID)
)^
-- end STATLOAD_STAT_BANK_HISTORY
-- begin STATLOAD_STAT_POKAZ
create table STATLOAD_STAT_POKAZ (
    ID uuid,
    VERSION integer not null,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    --
    FNAME varchar(100) not null,
    ID_POKAZ bigint not null,
    CODE varchar(20) not null,
    ID_FORM bigint,
    --
    primary key (ID)
)^
-- end STATLOAD_STAT_POKAZ
-- begin STATLOAD_STAT_FORM
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
)^
-- end STATLOAD_STAT_FORM
-- begin STATLOAD_VAR
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
    POSFIX varchar(255),
    CODE varchar(255) not null,
    NROW integer not null,
    NCOL integer not null,
    ID_STATPOKAZ uuid,
    ID_SPRAV uuid,
    STAT_KEY_FIELD varchar(255),
    STAT_RES_FIELD varchar(255),
    --
    primary key (ID)
)^
-- end STATLOAD_VAR
-- begin STATLOAD_STAT_SPRAV
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
    NAME_TABLE varchar(40) not null,
    ID_TABLE bigint,
    --
    primary key (ID)
)^
-- end STATLOAD_STAT_SPRAV
-- begin STATLOAD_DEPARTMENT
create table STATLOAD_DEPARTMENT (
    ID uuid,
    VERSION integer not null,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    --
    NAME varchar(512),
    CODE varchar(3),
    STAT_BANK_ID integer,
    REGION varchar(30),
    MFO varchar(255),
    --
    primary key (ID)
)^
-- end STATLOAD_DEPARTMENT
