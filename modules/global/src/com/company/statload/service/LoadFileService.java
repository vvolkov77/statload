package com.company.statload.service;

import com.company.statload.entity.Report;
import com.haulmont.cuba.core.entity.FileDescriptor;
import com.haulmont.cuba.core.global.FileStorageException;

import java.util.Date;

public interface LoadFileService {
    String NAME = "statload_LoadFileService";

    void loadtemplate(Report rep, FileDescriptor fdscr) throws FileStorageException;

    void loadmaping(Report rep, FileDescriptor fdscr) throws FileStorageException;

    boolean fieldisvalid(String field, String sprav);

    int getidsprav(Long id_stat_pokaz, Long id_stat_form, String schema);

    int checkstat(Date pdate, Report rep);

    void copystat(Date fromdate, Date todate, Report rep);

    void expstat(Date dateParam, Integer depParam, String region, String zoParam, Report rep, FileDescriptor fileDescriptor);

    int getDepartmentStatId(String depcode);

    String getDepartmentRegion(String depcode);

    void expstatidpokaz(Report rep, FileDescriptor fileDescriptor);
}