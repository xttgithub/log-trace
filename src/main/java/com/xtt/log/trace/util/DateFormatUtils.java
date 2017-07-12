package com.xtt.log.trace.util;


public class DateFormatUtils extends org.apache.commons.lang.time.DateFormatUtils{
    public static final String SIMPLE_PATTERN="yyyy-MM-dd HH:mm:ss.SSS";
    public static String format(Long millis){
        return format(millis,SIMPLE_PATTERN);
    }
}
