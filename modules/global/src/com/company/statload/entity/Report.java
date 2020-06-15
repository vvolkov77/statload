package com.company.statload.entity;

import com.haulmont.cuba.core.entity.BaseUuidEntity;
import com.haulmont.cuba.core.entity.Creatable;
import com.haulmont.cuba.core.entity.SoftDelete;
import com.haulmont.cuba.core.entity.Updatable;
import com.haulmont.cuba.core.entity.annotation.Lookup;
import com.haulmont.cuba.core.entity.annotation.LookupType;
import com.haulmont.cuba.core.entity.annotation.OnDeleteInverse;
import com.haulmont.cuba.core.global.DeletePolicy;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Table(name = "STATLOAD_REPORT")
@Entity(name = "statload_Report")
public class Report extends BaseUuidEntity implements Creatable, Updatable, SoftDelete {
    private static final long serialVersionUID = 6614055878265711215L;

    @NotNull(message = "{msg://statload_Report.name_Short.validation.NotNull}")
    @Column(name = "NAME_SHORT", length = 50)
    protected String name_Short;

    @Lob
    @Column(name = "NAME_LONG")
    protected String nameLong;

    @NotNull
    @Column(name = "VID", nullable = false)
    protected Integer vid;

    @OnDeleteInverse(DeletePolicy.DENY)
    @Lookup(type = LookupType.DROPDOWN, actions = "lookup")
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_STAT")
    protected StatForm ref_stat_form_id;

    @Column(name = "STAT_SCHEMA")
    protected String stat_schema;

    @Column(name = "CREATE_TS")
    protected Date createTs;

    @Column(name = "CREATED_BY", length = 50)
    protected String createdBy;

    @Column(name = "UPDATE_TS")
    protected Date updateTs;

    @Column(name = "UPDATED_BY", length = 50)
    protected String updatedBy;

    @Column(name = "DELETE_TS")
    protected Date deleteTs;

    @Column(name = "DELETED_BY", length = 50)
    protected String deletedBy;

    public StatSchemaEnum getStat_schema() {
        return stat_schema == null ? null : StatSchemaEnum.fromId(stat_schema);
    }

    public void setStat_schema(StatSchemaEnum stat_schema) {
        this.stat_schema = stat_schema == null ? null : stat_schema.getId();
    }

    public StatForm getRef_stat_form_id() {
        return ref_stat_form_id;
    }

    public void setRef_stat_form_id(StatForm ref_stat_form_id) {
        this.ref_stat_form_id = ref_stat_form_id;
    }

    @Override
    public Boolean isDeleted() {
        return deleteTs != null;
    }

    @Override
    public String getDeletedBy() {
        return deletedBy;
    }

    @Override
    public void setDeletedBy(String deletedBy) {
        this.deletedBy = deletedBy;
    }

    @Override
    public Date getDeleteTs() {
        return deleteTs;
    }

    @Override
    public void setDeleteTs(Date deleteTs) {
        this.deleteTs = deleteTs;
    }

    @Override
    public String getUpdatedBy() {
        return updatedBy;
    }

    @Override
    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    @Override
    public Date getUpdateTs() {
        return updateTs;
    }

    @Override
    public void setUpdateTs(Date updateTs) {
        this.updateTs = updateTs;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Date getCreateTs() {
        return createTs;
    }

    public void setCreateTs(Date createTs) {
        this.createTs = createTs;
    }

    public VidReportEnum getVid() {
        return vid == null ? null : VidReportEnum.fromId(vid);
    }

    public void setVid(VidReportEnum vid) {
        this.vid = vid == null ? null : vid.getId();
    }

    public String getNameLong() {
        return nameLong;
    }

    public void setNameLong(String nameLong) {
        this.nameLong = nameLong;
    }

    public String getName_Short() {
        return name_Short;
    }

    public void setName_Short(String name_Short) {
        this.name_Short = name_Short;
    }
}