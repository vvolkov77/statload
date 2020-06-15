update STATLOAD_VAR set NROW = 0 where NROW is null ;
alter table STATLOAD_VAR alter column NROW set not null ;
update STATLOAD_VAR set NCOL = 0 where NCOL is null ;
alter table STATLOAD_VAR alter column NCOL set not null ;
