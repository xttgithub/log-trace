package com.xtt.log.trace.util;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class TraceUtils {
    private static final Logger LOGGER_ACCESS = LoggerFactory.getLogger(TraceUtils.class);
    /**
     * 记录日志
     * @param msgMap
     * @param e
     */
    public static void trace(Map<String,Object> msgMap, Throwable e){
        String msg=GsonUtils.toJson(msgMap);
        if(e!=null)LOGGER_ACCESS.error(msg,e);
        else LOGGER_ACCESS.info(msg);
    }
}
