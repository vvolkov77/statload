create table S_FORMS (
    ID bigint,
    --
    "DAT_BEG" date,
    "DAT_END" date,
    "ID_P" bigint not null,
    "ID_PERIOD" bigint,
    NAME varchar(20),
    "NAME_FORM" varchar(256),
    "NAME_SHABLON" varchar(256),
    "PRIZ_AFN" boolean,
    "PRIZ_P" boolean,
    "PRIZ_SIZE" boolean,
    "PRIZ_TRANSFER" boolean,
    --
    primary key (ID)
);