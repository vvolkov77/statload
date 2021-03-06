-- begin STATLOAD_REPORT
alter table STATLOAD_REPORT add constraint FK_STATLOAD_REPORT_ON_ID_STAT foreign key (ID_STAT) references STATLOAD_STAT_FORM(ID)^
create index IDX_STATLOAD_REPORT_ON_ID_STAT on STATLOAD_REPORT (ID_STAT)^
-- end STATLOAD_REPORT
-- begin STATLOAD_VAR
alter table STATLOAD_VAR add constraint FK_STATLOAD_VAR_ON_ID_REPORT foreign key (ID_REPORT) references STATLOAD_REPORT(ID)^
alter table STATLOAD_VAR add constraint FK_STATLOAD_VAR_ON_ID_STATPOKAZ foreign key (ID_STATPOKAZ) references STATLOAD_STAT_POKAZ(ID)^
alter table STATLOAD_VAR add constraint FK_STATLOAD_VAR_ON_ID_SPRAV foreign key (ID_SPRAV) references STATLOAD_STAT_SPRAV(ID)^
create unique index IDX_STATLOAD_VAR_UK_CODE on STATLOAD_VAR (CODE) where DELETE_TS is null ^
create unique index IDX_STATLOAD_VAR_UNQ on STATLOAD_VAR (ID_REPORT, CODE) where DELETE_TS is null ^
create index IDX_STATLOAD_VAR_ON_ID_REPORT on STATLOAD_VAR (ID_REPORT)^
create index IDX_STATLOAD_VAR_ON_ID_STATPOKAZ on STATLOAD_VAR (ID_STATPOKAZ)^
create index IDX_STATLOAD_VAR_ON_ID_SPRAV on STATLOAD_VAR (ID_SPRAV)^
-- end STATLOAD_VAR
-- begin STATLOAD_STAT_FORM
create unique index IDX_STATLOAD_STAT_FORM_UK_ID_FORM on STATLOAD_STAT_FORM (ID_FORM) where DELETE_TS is null ^
-- end STATLOAD_STAT_FORM
-- begin STATLOAD_STAT_POKAZ
create unique index IDX_STATLOAD_STAT_POKAZ_UK_ID_POKAZ on STATLOAD_STAT_POKAZ (ID_POKAZ) where DELETE_TS is null ^
-- end STATLOAD_STAT_POKAZ
-- begin STATLOAD_STAT_SPRAV
create unique index IDX_STATLOAD_STAT_SPRAV_UK_ID_TABLE on STATLOAD_STAT_SPRAV (ID_TABLE) where DELETE_TS is null ^
-- end STATLOAD_STAT_SPRAV
