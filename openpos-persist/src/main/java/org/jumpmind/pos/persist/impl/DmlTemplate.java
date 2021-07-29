package org.jumpmind.pos.persist.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.apache.commons.text.lookup.StringLookup;
import org.jumpmind.pos.persist.PersistException;
import org.jumpmind.pos.persist.SqlStatement;
import org.jumpmind.pos.util.model.AbstractTypeCode;

import java.util.*;

public class DmlTemplate extends AbstractSqlTemplate implements Cloneable {

    String dml;

    public void setDml(String dml) {
        this.dml = dml;
    }

    public String getDml() {
        return dml;
    }

    public SqlStatement generateSQL(String sql, Map<String, Object> params) {
        List<String> keys = new ArrayList<>();
        StringSubstitutor literalSubstitution = new StringSubstitutor(new StringLookup() {
            @Override
            public String lookup(String key) {
                Object paramValue = params.get(key);
                return paramValue != null ? paramValue.toString() : "null";
            }
        }, "$${", "}", '\\');

        StringSubstitutor sub = new StringSubstitutor(new StringLookup() {
            @Override
            public String lookup(String key) {
                keys.add(key);
                return ":" + key;
            }
        });

        String preparedSql = literalSubstitution.replace(sql);
        preparedSql = sub.replace(preparedSql);

        String preppedWhereClause = literalSubstitution.replace(this.getWhere());
        preppedWhereClause = sub.replace(preppedWhereClause);

        StringBuilder buff = new StringBuilder();
        preparedSql = stripWhere(preparedSql);

        boolean hasWhereKeyword = false;

        buff.append(preparedSql);
        if (!StringUtils.isEmpty(preppedWhereClause)) {
            hasWhereKeyword = true;
            buff.append(" WHERE ");
            buff.append(preppedWhereClause);
        }

        for (String optionalWhereClause : this.getOptionalWhereClauses()) {
            Set<String> optionalWhereClauseKeys = new LinkedHashSet<>();
            String preppedOptionalWhereClause = literalSubstitution.replace(optionalWhereClause);

            StringSubstitutor optionalSubstitution = new StringSubstitutor(new StringLookup() {
                @Override
                public String lookup(String key) {
                    optionalWhereClauseKeys.add(key);
                    return ":" + key;
                }
            });

            preppedOptionalWhereClause = optionalSubstitution.replace(preppedOptionalWhereClause);

            boolean shouldInclude = true;
            for (String key : optionalWhereClauseKeys) {
                if (!params.containsKey(key)) {
                    shouldInclude = false;
                    break;
                }
            }

            if (shouldInclude) {
                if (!hasWhereKeyword) {
                    buff.append(" WHERE 1=1 ");
                    hasWhereKeyword = true;
                }
                buff.append(" AND (");

                buff.append(preppedOptionalWhereClause);
                buff.append(")");
                keys.addAll(optionalWhereClauseKeys);
            }
        }

        SqlStatement sqlStatement = new SqlStatement();
        sqlStatement.setSql(buff.toString());
        for (String key : keys) {
            Object value = params.get(key);
            if (value == null) {
                value = params.get("*");
                params.put(key, value);
            }
            if (value == null) {
                if (params.containsKey(key)) {
                    throw new PersistException(String.format(
                            "Required query parameter '%s' was present but the value is null. A value must be provided. Cannot build query: %s",
                            key, sqlStatement.getSql()));
                } else {
                    throw new PersistException(
                            String.format("Missing required query parameter '%s'. Cannot build query: %s", key, sqlStatement.getSql()));
                }
            } else if (value instanceof Boolean) {
                boolean bool = (Boolean) value;
                value = bool ? 1 : 0;
                params.put(key, value);
            } else if (value instanceof AbstractTypeCode) {
                value = ((AbstractTypeCode) value).value();
                params.put(key, value);
            }
        }
        if (params != null) {
            params.remove("*");
        }
        sqlStatement.setParameters(params);
        return sqlStatement;
    }

    public DmlTemplate copy() {
        try {
            return (DmlTemplate) this.clone();
        } catch (CloneNotSupportedException e) {
            throw new PersistException(e);
        }
    }

}
