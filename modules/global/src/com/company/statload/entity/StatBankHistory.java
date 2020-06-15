package com.company.statload.entity;

import com.haulmont.chile.core.annotations.NamePattern;
import com.haulmont.cuba.core.entity.StandardEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@NamePattern("%s|name")
@Table(name = "STATLOAD_STAT_BANK_HISTORY")
@Entity(name = "statload_StatBankHistory")
public class StatBankHistory extends StandardEntity {
    private static final long serialVersionUID = 8120999531680987405L;

    @NotNull
    @Column(name = "NAME", nullable = false, length = 256)
    protected String name;

    @Column(name = "MFO")
    protected String mfo;

    @NotNull
    @Column(name = "ID_FILIAL", nullable = false)
    protected Long id_filial;

    @NotNull
    @Column(name = "REGION", nullable = false)
    protected Long region;

    public String getMfo() {
        return mfo;
    }

    public void setMfo(String mfo) {
        this.mfo = mfo;
    }

    public Long getRegion() {
        return region;
    }

    public void setRegion(Long region) {
        this.region = region;
    }

    public Long getId_filial() {
        return id_filial;
    }

    public void setId_filial(Long id_filial) {
        this.id_filial = id_filial;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}