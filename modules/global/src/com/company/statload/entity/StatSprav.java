package com.company.statload.entity;

import com.haulmont.cuba.core.entity.StandardEntity;
import com.haulmont.cuba.core.entity.annotation.CaseConversion;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Table(name = "STATLOAD_STAT_SPRAV")
@Entity(name = "statload_StatSprav")
public class StatSprav extends StandardEntity {
    private static final long serialVersionUID = 3077926908004107769L;

    @CaseConversion
    @NotNull
    @Column(name = "NAME_TABLE", nullable = false, length = 40)
    protected String nameTable;

    @Column(name = "ID_TABLE", unique = true)
    protected Long id_table;

    public Long getId_table() {
        return id_table;
    }

    public void setId_table(Long id_table) {
        this.id_table = id_table;
    }

    public String getNameTable() {
        return nameTable;
    }

    public void setNameTable(String nameTable) {
        this.nameTable = nameTable;
    }
}