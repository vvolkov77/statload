alter table STATLOAD_REPORT rename column id_stat to id_stat__u52162 ;
drop index IDX_STATLOAD_REPORT_ON_ID_STAT ;
alter table STATLOAD_REPORT rename column version to version__u03515 ;
alter table STATLOAD_REPORT alter column version__u03515 drop not null ;
alter table STATLOAD_REPORT add column ID_STAT uuid ;
