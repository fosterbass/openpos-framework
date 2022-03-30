package org.jumpmind.pos.persist.impl;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.commons.io.IOUtils;
import org.jumpmind.db.sql.SqlException;
import org.jumpmind.exception.IoException;
import org.jumpmind.exception.ParseException;
import org.jumpmind.pos.persist.PersistException;
import org.jumpmind.util.FormatUtils;
import org.jumpmind.util.LinkedCaseInsensitiveMap;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.sql.Blob;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Data
public class DbRow {

    private Map<String, Object> columnValues = new LinkedHashMap<>();
    private CaseInsensitiveMap<String, String> columnsNamesIgnoreCase;

    public void setColumnValue(String columnName, Object value) {
        columnValues.put(columnName, value);
    }

    public Map<String, Object> getMap() {
        return columnValues;
    }

    public Set<String> getColumnNames() {
        return columnValues.keySet();
    }

    public boolean hasColumn(String columnName) {
        return columnValues.containsKey(columnName);
    }

    public Object getValueIgnoreCase(String columnName) {
        return columnValues.get(columnsNamesIgnoreCase.get(columnName));
    }

    public Object getValue(String columnName) {
        return columnValues.get(columnName);
    }

    public Object singleValue() {
        return columnValues.values().iterator().next();
    }

    public CaseInsensitiveMap<String, String> generateColumnsNamesIgnoreCase() {
        CaseInsensitiveMap<String, String> columnsNamesIgnoreCaseLocal = new CaseInsensitiveMap<>();
        columnValues.keySet().stream().forEach(k -> columnsNamesIgnoreCaseLocal.put(k, k));
        return columnsNamesIgnoreCaseLocal;

    }

    public Number numberValue() {
        Object obj = singleValue();
        if (obj != null) {
            if (obj instanceof Number) {
                return (Number) obj;
            } else {
                return new BigDecimal(obj.toString());
            }
        } else {
            return null;
        }
    }

    public Date dateValue() {
        Object obj = singleValue();
        if (obj != null) {
            if (obj instanceof Date) {
                return (Date) obj;
            } else {
                return Timestamp.valueOf(obj.toString());
            }
        } else {
            return null;
        }
    }

    public Long longValue() {
        Object obj = singleValue();
        if (obj != null) {
            if (obj instanceof Long) {
                return (Long) obj;
            } else if (obj instanceof Double) {
                return ((Double) obj).longValue();
            } else {
                return Long.valueOf(obj.toString());
            }
        } else {
            return null;
        }
    }

    public String stringValue() {
        Object obj = singleValue();
        if (obj != null) {
            return obj.toString();
        } else {
            return null;
        }
    }

    public byte[] getBytes(String columnName) {
        Object obj = getValue(columnName);
        return toBytes(obj);
    }

    public String getString(String columnName) {
        return getString(columnName, true);
    }

    public String getString(String columnName, boolean checkForColumn) {
        Object obj = this.getValue(columnName);
        if (obj != null) {
            if (obj instanceof String) {
                return (String) obj;
            } else if (obj instanceof BigDecimal) {
                return ((BigDecimal) obj).toPlainString();
            } else if (obj instanceof byte[]) {
                return Hex.encodeHexString((byte[]) obj);
            } else {
                return obj.toString();
            }
        } else {
            if (checkForColumn) {
                checkForColumn(columnName);
            }
            return null;
        }
    }

    public int getInt(String columnName) {
        Object obj = this.getValue(columnName);
        if (obj instanceof Number) {
            return ((Number) obj).intValue();
        } else if (obj instanceof String) {
            return Integer.parseInt(obj.toString());
        } else {
            checkForColumn(columnName);
            return 0;
        }
    }

    public long getLong(String columnName) {
        Object obj = this.getValue(columnName);
        if (obj instanceof Number) {
            return ((Number) obj).longValue();
        } else if (obj instanceof String) {
            return Long.parseLong(obj.toString());
        } else {
            checkForColumn(columnName);
            return 0;
        }
    }

    public float getFloat(String columnName) {
        Object obj = this.getValue(columnName);
        if (obj instanceof Number) {
            return ((Number) obj).floatValue();
        } else if (obj instanceof String) {
            return Float.parseFloat(obj.toString());
        } else {
            checkForColumn(columnName);
            return 0;
        }
    }

    public BigDecimal getBigDecimal(String columnName) {
        Object obj = this.getValue(columnName);
        if (obj instanceof BigDecimal) {
            return (BigDecimal) obj;
        } else if (obj instanceof String) {
            return new BigDecimal(obj.toString());
        } else if (obj instanceof Integer) {
            return new BigDecimal(((Integer) obj).intValue());
        } else {
            checkForColumn(columnName);
            return null;
        }
    }

    public boolean getBoolean(String columnName) {
        Object obj = this.getValue(columnName);
        if ("1".equals(obj)) {
            return true;
        } else if (obj instanceof Number) {
            int value = ((Number) obj).intValue();
            return value > 0 ? true : false;
        } else if (obj instanceof Boolean) {
            return (Boolean) obj;
        } else if (obj instanceof String) {
            return Boolean.parseBoolean((String) obj);
        } else {
            checkForColumn(columnName);
            return false;
        }
    }

    public Time getTime(String columnName) {
        Object obj = this.getValue(columnName);
        if (obj instanceof Time) {
            return (Time) obj;
        } else {
            Date date = getDateTime(columnName);
            return new Time(date.getTime());
        }
    }

    public Timestamp getTimestamp(String columnName) {
        Object obj = this.getValue(columnName);
        if (obj instanceof Timestamp) {
            return (Timestamp) obj;
        } else if (obj instanceof LocalDateTime) {
            return Timestamp.valueOf((LocalDateTime) obj);
        } else if (obj instanceof LocalDate) {
            return Timestamp.valueOf(((LocalDate) obj).atStartOfDay());
        } else {
            Date date = getDateTime(columnName);
            if (date != null) {
                return new Timestamp(date.getTime());
            }
            return null;
        }
    }

    final private java.util.Date getDate(String value, String[] pattern) {
        int spaceIndex = value.lastIndexOf(" ");
        int fractionIndex = value.lastIndexOf(".");
        if (spaceIndex > 0 && fractionIndex > 0 && value.substring(fractionIndex, value.length()).length() > 3) {
            return Timestamp.valueOf(value);
        } else {
            return FormatUtils.parseDate(value, pattern);
        }
    }

    public Date getDateTime(String columnName) {
        Object obj = this.getValue(columnName);
        if (obj instanceof Number) {
            long value = ((Number) obj).longValue();
            return new Date(value);
        } else if (obj instanceof Date) {
            return (Date) obj;
        } else if (obj instanceof String) {
            try {
                return getDate((String) obj, FormatUtils.TIMESTAMP_PATTERNS);
            } catch (ParseException ex) {
                // on xerial sqlite jdbc dates come back as longs
                return new Date(Long.parseLong((String) obj));
            }
        } else if (obj instanceof LocalDateTime) {
            return new Date(Timestamp.valueOf((LocalDateTime) obj).getTime());
        } else if (obj instanceof LocalDate) {
            return new Date(Timestamp.valueOf(((LocalDate) obj).atStartOfDay()).getTime());
        } else {
            checkForColumn(columnName);
            return null;
        }
    }
    protected void checkForColumn(String columnName) {
        if (!columnValues.containsKey(columnName)) {
            throw new PersistException("Column name not found in result set: '" + columnName + "'");
        }
    }

    protected byte[] toBytes(Object obj) {
        if (obj != null) {
            if (obj instanceof byte[]) {
                return (byte[]) obj;
            } else if (obj instanceof Blob) {
                Blob blob = (Blob) obj;
                try {
                    return IOUtils.toByteArray(blob.getBinaryStream());
                } catch (IOException e) {
                    throw new IoException(e);
                } catch (SQLException e) {
                    throw new SqlException(e);
                }
            } else if (obj instanceof String) {
                return obj.toString().getBytes(Charset.defaultCharset());
            } else {
                throw new IllegalStateException(String.format(
                        "Cannot translate a %s into a byte[]", obj.getClass().getName()));
            }
        } else {
            return null;
        }
    }

}
