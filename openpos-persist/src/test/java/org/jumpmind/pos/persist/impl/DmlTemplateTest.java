package org.jumpmind.pos.persist.impl;

import org.jumpmind.pos.persist.SqlStatement;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class DmlTemplateTest {
    private DmlTemplate dmlTemplate;
    private Map<String, Object> params;
    private static final String DEFAULT_DML = "update bar set foo = ${foo}";

    @Before
    public void setup() {
        dmlTemplate = new DmlTemplate();
        dmlTemplate.setDml(DEFAULT_DML);
        params = new HashMap<>();
        params.put("foo", 1);
    }

    @Test
    public void generateSql() {
        SqlStatement sqlStatement = dmlTemplate.generateSQL(DEFAULT_DML, params);
        assertEquals("update bar set foo = :foo", sqlStatement.getSql());
        assertEquals(1, sqlStatement.getParameters().getValues().size());
        assertEquals(1, sqlStatement.getParameters().getValues().get("foo"));
    }

    @Test
    public void generateSQLWithInClause() {
        dmlTemplate.setWhere("baz in ( a, b )");
        SqlStatement sqlStatement = dmlTemplate.generateSQL(DEFAULT_DML, params);
        assertEquals("update bar set foo = :foo WHERE baz in ( a, b )", sqlStatement.getSql());
        assertEquals(1, sqlStatement.getParameters().getValues().size());
        assertEquals(1, sqlStatement.getParameters().getValues().get("foo"));
    }

    @Test
    public void generateSQLWithInClauseAndParameters() {
        dmlTemplate.setWhere("baz in ( ${para} )");
        params.put("para", "a");
        SqlStatement sqlStatement = dmlTemplate.generateSQL(DEFAULT_DML, params);
        assertEquals("update bar set foo = :foo WHERE baz in ( :para )", sqlStatement.getSql());
        assertEquals(2, sqlStatement.getParameters().getValues().size());
        assertEquals(1, sqlStatement.getParameters().getValues().get("foo"));
        assertEquals("a", sqlStatement.getParameters().getValues().get("para"));
    }

    @Test
    public void generateSQLWithInClauseAndParametersAsList() {
        dmlTemplate.setWhere("baz in ( ${para} )");
        params.put("para", Arrays.asList("a", "b"));
        SqlStatement sqlStatement = dmlTemplate.generateSQL(DEFAULT_DML, params);
        assertEquals("update bar set foo = :foo WHERE baz in ( :para )", sqlStatement.getSql());
        assertEquals(2, sqlStatement.getParameters().getValues().size());
        assertEquals(1, sqlStatement.getParameters().getValues().get("foo"));
        assertEquals(Arrays.asList("a", "b"), sqlStatement.getParameters().getValues().get("para"));
    }

    @Test
    public void generateSQLWithInClauseAndParametersAsArray() {
        dmlTemplate.setWhere("baz in ( ${para} )");
        params.put("para", new String[]{"a", "b"});
        SqlStatement sqlStatement = dmlTemplate.generateSQL(DEFAULT_DML, params);
        assertEquals("update bar set foo = :foo WHERE baz in ( :para )", sqlStatement.getSql());
        assertEquals(2, sqlStatement.getParameters().getValues().size());
        assertEquals(1, sqlStatement.getParameters().getValues().get("foo"));
        assertArrayEquals(new String[]{"a", "b"}, (String[]) sqlStatement.getParameters().getValues().get("para"));
    }
}
