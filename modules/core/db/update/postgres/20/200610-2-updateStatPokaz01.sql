update STATLOAD_STAT_POKAZ set FNAME = '' where FNAME is null ;
alter table STATLOAD_STAT_POKAZ alter column FNAME set not null ;
