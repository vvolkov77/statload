create table S_OTRASL (
    "ID_P" bigint,
    --
    CODE varchar(3) not null,
    "DAT_BEG" date,
    "DAT_END" date,
    "ID_GR_OTRASL" bigint,
    ID bigint not null,
    NAME varchar(256),
    --
    primary key ("ID_P")
);