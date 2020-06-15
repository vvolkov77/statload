update STATLOAD_STAT_BANK_HISTORY set REGION = 0 where REGION is null ;
alter table STATLOAD_STAT_BANK_HISTORY alter column REGION set not null ;
