package com.company.statload.entity;

import com.haulmont.cuba.core.entity.StandardEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Table(name = "STATLOAD_STAT_FORM")
@Entity(name = "statload_StatForm")
public class StatForm extends StandardEntity {
    private static final long serialVersionUID = 1813030609111334475L;

    @Column(name = "ID_FORM", unique = true)
    protected Long id_form;

    @Column(name = "SHORT_NAME")
    protected String short_name;

    @Column(name = "LONG_NAME", length = 512)
    protected String long_name;

    public String getLong_name() {
        return long_name;
    }

    public void setLong_name(String long_name) {
        this.long_name = long_name;
    }

    public String getShort_name() {
        return short_name;
    }

    public void setShort_name(String short_name) {
        this.short_name = short_name;
    }

    public Long getId_form() {
        return id_form;
    }

    public void setId_form(Long id_form) {
        this.id_form = id_form;
    }
}