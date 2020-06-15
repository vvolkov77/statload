package com.company.statload.entity;

import com.haulmont.cuba.core.entity.StandardEntity;
import com.haulmont.cuba.core.entity.annotation.Lookup;
import com.haulmont.cuba.core.entity.annotation.LookupType;
import com.haulmont.cuba.core.entity.annotation.OnDeleteInverse;
import com.haulmont.cuba.core.global.DeletePolicy;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Table(name = "STATLOAD_VAR", indexes = {
        @Index(name = "IDX_STATLOAD_VAR_UNQ", columnList = "ID_REPORT, CODE", unique = true)
})
@Entity(name = "statload_Var")
public class Var extends StandardEntity {
    private static final long serialVersionUID = 6330831237182417930L;

    @OnDeleteInverse(DeletePolicy.CASCADE)
    @Lookup(type = LookupType.DROPDOWN, actions = "lookup")
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_REPORT")
    protected Report ref_report;

    @Column(name = "POSFIX")
    protected String posfix;

    @NotNull
    @Column(name = "CODE", nullable = false, unique = true)
    protected String code;

    @NotNull
    @Column(name = "NROW", nullable = false)
    protected Integer nrow;

    @NotNull
    @Column(name = "NCOL", nullable = false)
    protected Integer ncol;

    @OnDeleteInverse(DeletePolicy.DENY)
    @Lookup(type = LookupType.DROPDOWN, actions = "lookup")
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_STATPOKAZ")
    protected StatPokaz ref_pokaz;

    @OnDeleteInverse(DeletePolicy.DENY)
    @Lookup(type = LookupType.DROPDOWN, actions = "lookup")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_SPRAV")
    protected StatSprav ref_sprav;

    @Column(name = "STAT_KEY_FIELD")
    protected String stat_key_field;

    @Column(name = "STAT_RES_FIELD")
    protected String stat_res_field;

    public String getPosfix() {
        return posfix;
    }

    public void setPosfix(String posfix) {
        this.posfix = posfix;
    }

    public String getStat_res_field() {
        return stat_res_field;
    }

    public void setStat_res_field(String stat_res_field) {
        this.stat_res_field = stat_res_field;
    }

    public String getStat_key_field() {
        return stat_key_field;
    }

    public void setStat_key_field(String stat_key_field) {
        this.stat_key_field = stat_key_field;
    }

    public StatSprav getRef_sprav() {
        return ref_sprav;
    }

    public void setRef_sprav(StatSprav ref_sprav) {
        this.ref_sprav = ref_sprav;
    }

    public StatPokaz getRef_pokaz() {
        return ref_pokaz;
    }

    public void setRef_pokaz(StatPokaz ref_pokaz) {
        this.ref_pokaz = ref_pokaz;
    }

    public Integer getNcol() {
        return ncol;
    }

    public void setNcol(Integer ncol) {
        this.ncol = ncol;
    }

    public Integer getNrow() {
        return nrow;
    }

    public void setNrow(Integer nrow) {
        this.nrow = nrow;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Report getRef_report() {
        return ref_report;
    }

    public void setRef_report(Report ref_report) {
        this.ref_report = ref_report;
    }
}