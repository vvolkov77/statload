create table DETAIL (
    ID bigint,
    --
    "D_REPORT" date,
    "ID_FORM" bigint,
    "ID_POKAZ" bigint,
    LINE integer,
    "PR_PERIOD" bigint,
    ZNAC decimal(16, 4),
    ZO boolean,
    --
    primary key (ID)
);