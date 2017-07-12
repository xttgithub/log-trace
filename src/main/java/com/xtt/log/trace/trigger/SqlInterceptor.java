package com.xtt.log.trace.trigger;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.xtt.log.trace.Constants;
import com.xtt.log.trace.Ignore;
import com.xtt.log.trace.MsgType;
import com.xtt.log.trace.helper.TraceHolder;
import com.xtt.log.trace.util.DateFormatUtils;
import com.xtt.log.trace.util.TraceUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.time.StopWatch;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.springframework.core.annotation.AnnotationUtils;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.regex.Matcher;


/**
 * 注解拦截接口的方法
 * Executor (update, query, flushStatements, commit, rollback, getTransaction, close, isClosed)
 * ParameterHandler (getParameterObject, setParameters)
 * ResultSetHandler (handleResultSets, handleOutputParameters)
 * StatementHandler (prepare, parameterize, batch, update, query)
 */
@Intercepts({
        @Signature(type = Executor.class, method = "update", args = { MappedStatement.class, Object.class }),
        @Signature(type = Executor.class, method = "query", args = { MappedStatement.class, Object.class,
                RowBounds.class, ResultHandler.class }) })
public class SqlInterceptor implements Interceptor {
    private Properties properties;
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        if(AnnotationUtils.findAnnotation(invocation.getMethod(),Ignore.class)!=null){
            return invocation.proceed();
        }
        MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
        Object parameter = null;
        if (invocation.getArgs().length > 1) {
            parameter = invocation.getArgs()[1];
        }
        String sqlId = mappedStatement.getId();
        BoundSql boundSql = mappedStatement.getBoundSql(parameter);
        Configuration configuration = mappedStatement.getConfiguration();
        Object ret = null;
        Throwable ex=null;
        StopWatch stopWatch=new StopWatch();
        try {
            stopWatch.start();
            TraceHolder.incrLogicId();
            ret = invocation.proceed();
            stopWatch.stop();
            return ret;
        } catch (Throwable e) {
            stopWatch.stop();
            ex=e;
            throw e;
        }finally {
            TraceUtils.trace(MapUtils.putAll(Maps.newLinkedHashMap(),new Object[]{
                    Constants.TRACE_TIME, DateFormatUtils.format(stopWatch.getStartTime()),
                    Constants.MSG_TYPE, MsgType.sql.name(),
                    Constants.CONSUMED_TIME,Long.toString(stopWatch.getTime()),
                    Constants.LOCATION,ImmutableMap.of(
                        Constants.CLASS,invocation.getTarget().getClass().getName(),
                        Constants.METHOD,invocation.getMethod().getName(),
                        Constants.SQLID,sqlId,
                        Constants.SQL,showSql(configuration, boundSql)
                    ),
                    Constants.ARGS, parameter,
                    Constants.RESULT,ret
            }),ex);
            TraceHolder.removeLogicId();
        }
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public static String showSql(Configuration configuration, BoundSql boundSql) {
        Object parameterObject = boundSql.getParameterObject();
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        String sql = boundSql.getSql().replaceAll("[\\s]+", " ");
        try {
            if (parameterMappings.size() > 0 && parameterObject != null) {
                TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
                if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
                    sql = sql.replaceFirst("\\?", getParameterValue(parameterObject));

                } else {
                    MetaObject metaObject = configuration.newMetaObject(parameterObject);
                    for (ParameterMapping parameterMapping : parameterMappings) {
                        String propertyName = parameterMapping.getProperty();
                        if (metaObject.hasGetter(propertyName)) {
                            Object obj = metaObject.getValue(propertyName);
                            sql = sql.replaceFirst("\\?", getParameterValue(obj));
                        } else if (boundSql.hasAdditionalParameter(propertyName)) {
                            Object obj = boundSql.getAdditionalParameter(propertyName);
                            sql = sql.replaceFirst("\\?", getParameterValue(obj));
                        }
                    }
                }
            }
        }catch (Exception e){}
        return sql;
    }

    private static String getParameterValue(Object obj) {
        String value = null;
        if (obj instanceof String) {
            value = "'" + obj.toString() + "'";
        } else if (obj instanceof Date) {
            DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.CHINA);
            value = "'" + formatter.format(new Date()) + "'";
        } else {
            if (obj != null) {
                value = obj.toString();
            } else {
                value = "";
            }

        }
        return Matcher.quoteReplacement(value);
    }
}
