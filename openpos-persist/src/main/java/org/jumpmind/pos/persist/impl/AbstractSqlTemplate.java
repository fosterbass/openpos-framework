package org.jumpmind.pos.persist.impl;

import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractSqlTemplate {
    private String name;
    private String where;
    private List<String> optionalWhereClauses;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWhere() {
        return where;
    }

    public void setWhere(String where) {
        this.where = where;
    }

    public boolean hasWhere() {
        return (this.where != null);
    }

    public List<String> getOptionalWhereClauses() {
        if (optionalWhereClauses == null) {
            optionalWhereClauses = new ArrayList<>();
        }
        return optionalWhereClauses;
    }

    public void setOptionalWhereClauses(List<String> optionalWhereClauses) {
        this.optionalWhereClauses = optionalWhereClauses;
    }

    public boolean hasOptionalWhereClauses() {
        return !getOptionalWhereClauses().isEmpty();
    }

    protected String stripWhere(String preppedSql) {
        Assert.notNull(preppedSql, "preppedSql must be non-null.");
        String sqlTrimmed = preppedSql.trim();
        if (sqlTrimmed.endsWith("WHERE") || sqlTrimmed.endsWith("where")) {
            return sqlTrimmed.substring(0, sqlTrimmed.length() - "where".length());
        } else {
            return sqlTrimmed;
        }
    }

}
