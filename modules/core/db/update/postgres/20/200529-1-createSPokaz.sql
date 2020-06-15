create table S_POKAZ (
    "ID_P" bigint,
    --
    AP varchar(2),
    "CODE_POKAZ" varchar(20),
    "DAT_BEG" date,
    "DAT_END" date,
    GR boolean,
    "ID_FORM" bigint,
    ID bigint,
    "NAME_POKAZ" varchar(1500),
    "NAME_POKAZ_KZ" varchar(1500),
    RP boolean,
    VP varchar(3),
    --
    primary key ("ID_P")
);