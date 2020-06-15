package com.company.statload.entity;

import com.haulmont.chile.core.datatypes.impl.EnumClass;

import javax.annotation.Nullable;


public enum StatSchemaEnum implements EnumClass<String> {

    STAT("STAT"),
    DIS("DIS");

    private String id;

    StatSchemaEnum(String value) {
        this.id = value;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public static StatSchemaEnum fromId(String id) {
        for (StatSchemaEnum at : StatSchemaEnum.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}