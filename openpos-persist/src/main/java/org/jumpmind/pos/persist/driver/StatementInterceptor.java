/**
 * Licensed to JumpMind Inc under one or more contributor
 * license agreements.  See the NOTICE file distributed
 * with this work for additional information regarding
 * copyright ownership.  JumpMind Inc licenses this file
 * to you under the GNU General Public License, version 3.0 (GPLv3)
 * (the "License"); you may not use this file except in compliance
 * with the License.
 * <p>
 * You should have received a copy of the GNU General Public License,
 * version 3.0 (GPLv3) along with this library; if not, see
 * http://www.gnu.org/licenses.
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jumpmind.pos.persist.driver;

import org.jumpmind.db.sql.LogSqlBuilder;
import org.jumpmind.properties.TypedProperties;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class StatementInterceptor extends WrapperInterceptor {
    public static final String LONG_RUNNING_THRESHOLD_PROPERTY = "jumpmind.commerce.longRunningThreshold";

    protected final List<Object> psArgs = new ArrayList<>();
    protected final LogSqlBuilder sqlBuilder = new LogSqlBuilder();

    protected long longRunningThreshold = 20000;

    private String sqlKey;

    public StatementInterceptor(Object wrapped, TypedProperties systemPlusEngineProperties) {
        super(wrapped);
        String longRunningThresholdString = systemPlusEngineProperties.get(LONG_RUNNING_THRESHOLD_PROPERTY);

        if (!StringUtils.isEmpty(longRunningThresholdString)) {
            longRunningThreshold = Long.parseLong(longRunningThresholdString.trim());
            log.debug("Long Running SQL threshold is: {}ms.", longRunningThreshold);
        }
    }

    @Override
    public InterceptResult preExecute(String methodName, Object... parameters) {
        if (getWrapped() instanceof PreparedStatementWrapper) {
            return preparedStatementPreExecute((PreparedStatementWrapper) getWrapped(), methodName, parameters);
        }
        return new InterceptResult();
    }

    protected InterceptResult preparedStatementPreExecute(PreparedStatementWrapper ps, String methodName, Object[] parameters) {
        if (methodName.startsWith("set") && (parameters != null && parameters.length > 1)) {
            psArgs.add(("setNull".equalsIgnoreCase(methodName)) ? null : parameters[1]);
        }

        if (methodName.startsWith("execute")) {
            sqlKey = generateKey(ps);
            SqlWatchdog.sqlBegin(sqlKey,
                    new InProgressSqlStatement(ps.getStatement(), psArgs, System.currentTimeMillis(), Thread.currentThread().getName()));
        }

        return new InterceptResult();
    }

    private String generateKey(PreparedStatementWrapper ps) {
        return Thread.currentThread().getId() + ps.getStatement();
    }

    @Override
    public InterceptResult postExecute(String methodName, Object result, long startTime, long endTime, Object... parameters) {
        if (getWrapped() instanceof PreparedStatementWrapper) {
            return preparedStatementPostExecute((PreparedStatementWrapper) getWrapped(), methodName, result, startTime, endTime, parameters);
        }
        else if (getWrapped() instanceof StatementWrapper) {
            return statementPostExecute((StatementWrapper) getWrapped(), methodName, result, startTime, endTime, parameters);
        }
        else {
            return new InterceptResult();
        }
    }

    public InterceptResult preparedStatementPostExecute(PreparedStatementWrapper ps, String methodName, Object result, long startTime, long endTime,
            Object... parameters) {

        if (methodName.startsWith("execute")) {
            long elapsed = endTime - startTime;
            preparedStatementExecute(methodName, elapsed, ps.getStatement(), psArgs.toArray());
            psArgs.clear();
        }

        return new InterceptResult();
    }

    public InterceptResult statementPostExecute(StatementWrapper ps, String methodName, Object result, long startTime, long endTime,
            Object... parameters) {

        if (methodName.startsWith("execute")) {
            long elapsed = endTime - startTime;
            statementExecute(methodName, elapsed, parameters);
        }

        return new InterceptResult();
    }

    public void preparedStatementExecute(String methodName, long elapsed, String sql, Object[] args) {
        if (elapsed > longRunningThreshold) {
            String dynamicSql = sqlBuilder.buildDynamicSqlForLog(sql, args, null);
            log.warn("Long Running ({}ms.) {}", elapsed, dynamicSql.trim());
        }

        if (log.isInfoEnabled()) {
            String dynamicSql = sqlBuilder.buildDynamicSqlForLog(sql, args, null);
            log.info("PreparedStatement.{} ({}ms.) {}", methodName, elapsed, dynamicSql.trim());
        }
    }

    public void statementExecute(String methodName, long elapsed, Object... parameters) {
        if (elapsed > longRunningThreshold) {
            log.warn("Long Running ({}ms.) {}", elapsed, Arrays.toString(parameters));
        }

        if (log.isInfoEnabled()) {
            log.info("Statement.{} ({}ms.) {}", methodName, elapsed, Arrays.toString(parameters));
        }
    }

    @Override
    public void cleanupExecute(String methodName, Exception thrownException) {
        SqlWatchdog.sqlEnd(sqlKey);
        if (thrownException != null
                && log.isDebugEnabled()
                && (getWrapped() instanceof PreparedStatementWrapper)) {

            PreparedStatementWrapper ps = (PreparedStatementWrapper) getWrapped();
            String sql = sqlBuilder.buildDynamicSqlForLog(ps.getStatement(), psArgs.toArray(), null);

            log.debug("SQL Caused Exception " + sql, thrownException);
        }
    }
}
