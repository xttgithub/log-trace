package com.xtt.log.trace.log4j;

import com.google.common.collect.Maps;
import com.xtt.log.trace.Constants;
import com.xtt.log.trace.MsgType;
import com.xtt.log.trace.util.DateFormatUtils;
import com.xtt.log.trace.util.GsonUtils;
import com.xtt.log.trace.util.RequestUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Layout;
import org.apache.log4j.helpers.PatternConverter;
import org.apache.log4j.helpers.PatternParser;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;

import java.util.HashMap;
import java.util.Map;

public class JsonPatternLayout extends Layout {
    private final static String DEFAULT_CONVERSION_PATTERN ="%m%n";
    private final int BUF_SIZE = 256;
    private final int MAX_CAPACITY = 1024;
    private StringBuffer sbuf = new StringBuffer(BUF_SIZE);
    private String pattern;
    private PatternConverter head;

    public JsonPatternLayout() {
        this(DEFAULT_CONVERSION_PATTERN);
    }
    public JsonPatternLayout(String pattern) {
        pattern=pattern.replace("%m%n","");
        this.pattern = pattern;
        head = createPatternParser((pattern == null) ? DEFAULT_CONVERSION_PATTERN :
                pattern).parse();
    }
    public void setConversionPattern(String conversionPattern) {
        conversionPattern=conversionPattern.replace("%m%n","");
        pattern = conversionPattern;
        head = createPatternParser(conversionPattern).parse();
    }

    public  String getConversionPattern() {
        return pattern;
    }
    protected PatternParser createPatternParser(String pattern) {
        return new PatternParser(pattern);
    }

    public String format(LoggingEvent loggingEvent) {
        if(sbuf.capacity() > MAX_CAPACITY) {
            sbuf = new StringBuffer(BUF_SIZE);
        } else {
            sbuf.setLength(0);
        }
        PatternConverter c = head;
        while(c != null) {
            c.format(sbuf, loggingEvent);
            c = c.next;
        }
        return sbuf.toString()+getJsonMessage(loggingEvent);
    }

    private String getJsonMessage(LoggingEvent loggingEvent){
        Map<String, Object> contentMap = Maps.newLinkedHashMap();
        try {
            String ndc=StringUtils.defaultIfEmpty(loggingEvent.getNDC(),"");
            String logicId=StringUtils.contains(ndc," ")?StringUtils.substringAfterLast(ndc," "):ndc;
            MapUtils.putAll(contentMap,new Object[]{
                    Constants.TRACE_TIME, DateFormatUtils.format(System.currentTimeMillis()),
                    Constants.TRACE_ID,MapUtils.getString(loggingEvent.getProperties(),Constants.TRACE_ID,""),
                    Constants.SPAN_ID,MapUtils.getString(loggingEvent.getProperties(),Constants.SPAN_ID,""),
                    Constants.LOGIC_ID, logicId,
                    Constants.HOSTIP, RequestUtils.getHostIp()
            });
            //添加Message
            String message=loggingEvent.getRenderedMessage();
            if(StringUtils.equals(loggingEvent.getLoggerName(),"TraceUtils")){//跟踪日志
                try {
                    contentMap.putAll(GsonUtils.fromJson(message));
                }catch (Exception e){
                }
            }else{//普通日志
                contentMap.put(Constants.MSG_TYPE, MsgType.plain.name());
                contentMap.put(Constants.MESSAGE,message);
            }
            //添加异常信息
            ThrowableInformation throwableInformation=loggingEvent.getThrowableInformation();
            if (throwableInformation != null) {
                contentMap.put(Constants.EXCEPTION, getExceptionMessage(throwableInformation));
            }
            return GsonUtils.toJson(contentMap) + "\n";
        }catch (Exception e){
            return GsonUtils.toJson(MapUtils.putAll(contentMap,new Object[]{
                    Constants.EXCEPTION,getExceptionMessage(new ThrowableInformation(e))
            }));
        }
    }
    private Map getExceptionMessage(ThrowableInformation throwableInformation){
        Map<String, Object> exceptionInformation = new HashMap<String, Object>();
        if (throwableInformation.getThrowable().getClass().getCanonicalName() != null) {
            exceptionInformation.put(Constants.EXCEPTIONCLASS, throwableInformation.getThrowable().getClass().getCanonicalName());
        }
        if (throwableInformation.getThrowable().getMessage() != null) {
            exceptionInformation.put(Constants.EXCEPTIONMESSAGE, throwableInformation.getThrowable().getMessage());
        }
        if (throwableInformation.getThrowableStrRep() != null) {
            String stackTrace = StringUtils.join(throwableInformation.getThrowableStrRep(), "\n");
            exceptionInformation.put(Constants.STACKTRACE, stackTrace);
        }
        return exceptionInformation;
    }

    public boolean ignoresThrowable() {
        return false;
    }

    public void activateOptions() {}
}