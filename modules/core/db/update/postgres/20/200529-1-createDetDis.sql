create table DET_DIS (
    ID bigint,
    --
    "D_REPORT" date not null,
    "ID_FILIAL" bigint,
    "ID_FORM" bigint,
    "ID_POKAZ" bigint not null,
    "ID_REGION" bigint,
    LINE integer,
    "PR_PERIOD" bigint,
    ZNAC decimal(16, 4),
    ZO boolean,
    --
    primary key (ID)
);