package com.company.statload.entity.key;

import com.haulmont.chile.core.annotations.MetaClass;
import com.haulmont.cuba.core.entity.EmbeddableEntity;
import com.haulmont.cuba.core.global.DesignSupport;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;
import java.util.Objects;

@DesignSupport("{'imported':true}")
@MetaClass(name = "statload_PFileCompKey")
@Embeddable
public class PFileCompKey extends EmbeddableEntity {
    private static final long serialVersionUID = -119991672338263843L;
    @Temporal(TemporalType.DATE)
    @Column(name = "DREP")
    protected Date drep;
    @Column(name = "\"ID_REP\"")
    protected Long idRep;

    @Override
    public int hashCode() {
        return Objects.hash(idRep, drep);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PFileCompKey entity = (PFileCompKey) o;
        return Objects.equals(this.idRep, entity.idRep) &&
                Objects.equals(this.drep, entity.drep);
    }

    public Long getIdRep() {
        return idRep;
    }

    public void setIdRep(Long idRep) {
        this.idRep = idRep;
    }

    public Date getDrep() {
        return drep;
    }

    public void setDrep(Date drep) {
        this.drep = drep;
    }
}