create table S_UNPAYMENT (
    "ID_P" bigint,
    --
    CODE varchar(2) not null,
    "DAT_BEG" date,
    "DAT_END" date,
    ID bigint not null,
    NAME varchar(256),
    --
    primary key ("ID_P")
);