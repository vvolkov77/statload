package com.company.statload.entity;

import com.haulmont.chile.core.annotations.NamePattern;
import com.haulmont.cuba.core.entity.StandardEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@NamePattern("%s|fname")
@Table(name = "STATLOAD_STAT_POKAZ")
@Entity(name = "statload_StatPokaz")
public class StatPokaz extends StandardEntity {
    private static final long serialVersionUID = -4918729128116525350L;

    @NotNull
    @Column(name = "FNAME", nullable = false, length = 100)
    protected String fname;

    @NotNull
    @Column(name = "ID_POKAZ", nullable = false, unique = true)
    protected Long id_pokaz;

    @NotNull
    @Column(name = "CODE", nullable = false, length = 20)
    protected String code;

    @Column(name = "ID_FORM")
    protected Long id_form;

    public String getFname() {
        return fname;
    }

    public void setFname(String fname) {
        this.fname = fname;
    }

    public Long getId_form() {
        return id_form;
    }

    public void setId_form(Long id_form) {
        this.id_form = id_form;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Long getId_pokaz() {
        return id_pokaz;
    }

    public void setId_pokaz(Long id_pokaz) {
        this.id_pokaz = id_pokaz;
    }

}