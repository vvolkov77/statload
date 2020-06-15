package com.company.statload.entity;

import com.haulmont.chile.core.annotations.NamePattern;
import com.haulmont.cuba.core.entity.StandardEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Table(name = "STATLOAD_DEPARTMENT")
@NamePattern("%s|name")
@Entity(name = "statload_Department")
public class Department extends StandardEntity {
    private static final long serialVersionUID = -3456720803479450088L;

    @Column(name = "NAME", length = 512)
    protected String name;

    @Column(name = "CODE", length = 3)
    protected String code;

    @Column(name = "STAT_BANK_ID")
    protected Integer stat_bank_id;

    @Column(name = "REGION", length = 30)
    protected String region;

    @Column(name = "MFO")
    protected String mfo;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMfo() {
        return mfo;
    }

    public void setMfo(String mfo) {
        this.mfo = mfo;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public Integer getStat_bank_id() {
        return stat_bank_id;
    }

    public void setStat_bank_id(Integer stat_bank_id) {
        this.stat_bank_id = stat_bank_id;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}