package com.company.statload.entity;

import com.haulmont.chile.core.datatypes.impl.EnumClass;

import javax.annotation.Nullable;


public enum VidReportEnum implements EnumClass<Integer> {

    VARLEN(1),
    MATRIX(2);

    private Integer id;

    VidReportEnum(Integer value) {
        this.id = value;
    }

    public Integer getId() {
        return id;
    }

    @Nullable
    public static VidReportEnum fromId(Integer id) {
        for (VidReportEnum at : VidReportEnum.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}