create table REP_FILE (
    ID bigint,
    --
    "D_CREATE" date,
    "D_REPORT" date,
    "ID_FORM" bigint,
    STATUS boolean,
    ZO varchar(256),
    --
    primary key (ID)
);