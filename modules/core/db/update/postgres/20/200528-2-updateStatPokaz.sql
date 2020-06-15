update STATLOAD_STAT_POKAZ set ID_POKAZ = 0 where ID_POKAZ is null ;
alter table STATLOAD_STAT_POKAZ alter column ID_POKAZ set not null ;
update STATLOAD_STAT_POKAZ set CODE = '' where CODE is null ;
alter table STATLOAD_STAT_POKAZ alter column CODE set not null ;
