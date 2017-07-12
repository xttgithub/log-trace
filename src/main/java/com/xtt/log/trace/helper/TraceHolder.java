package com.xtt.log.trace.helper;

import com.xtt.log.trace.Constants;
import com.xtt.log.trace.Trace;
import com.xtt.log.trace.util.NumberUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.UUID;

/**
 * Trace操作
 */
public class TraceHolder {

    /**
     * 获取“要发送的跟踪信息”
     */
    public static Trace getSendingTrace(){
        Trace trace=getTrace();
        //spanId签名
        trace.setSpanId(getSignSpanId(trace.getSpanId()));
        //logicId置空
        trace.setLogicId(null);
        return trace;
    }

    /**
     * 获取当前的跟踪信息
     * @return
     */
    public static Trace getTrace(){
        String traceId= ContextHolder.getContextString(Constants.TRACE_ID);
        String spanId= ContextHolder.getContextString(Constants.SPAN_ID);
        String logicId= ContextHolder.getContextString(Constants.LOGIC_ID);
        return Trace.of(traceId,spanId,logicId);
    }

    /**
     * 设置上下文
     * @param trace
     */
    public static void setContext(Trace trace){
        if(trace==null|| StringUtils.isBlank(trace.getTraceId())){
            trace=Trace.of(TraceHolder.getDefaultTraceId());
        }
        ContextHolder.setContext(Constants.TRACE_ID,trace.getTraceId());
        ContextHolder.setContext(Constants.SPAN_ID,StringUtils.defaultIfBlank(trace.getSpanId(),"0"));
        ContextHolder.setContext(Constants.LOGIC_ID,StringUtils.defaultIfBlank(trace.getLogicId(),"0"));
    }

    /**
     * 清除上下文
     */
    public static void cleanContext(){
        ContextHolder.clearContext();
    }


    /**
     * 获取默认的traceId
     * @return
     */
    public static  String getDefaultTraceId(){
        return StringUtils.remove(UUID.randomUUID().toString(),'-');
    }

    /**
     * 获取当前traceId
     * @return
     */
    public static String getTraceId(){
        return ContextHolder.getContextString(Constants.TRACE_ID);
    }
    /**
     * 获取当前spanId
     * @return
     */
    public static String getSpanId(){
        return ContextHolder.getContextString(Constants.SPAN_ID);
    }

    /**
     * 获取当前logicId
     * @return
     */
    public static String getLogicId(){
        return ContextHolder.getContextString(Constants.LOGIC_ID);
    }

    /**
     * 累加logicId
     */
    public static void incrLogicId(){
        ContextHolder.incrContext(Constants.LOGIC_ID);
    }

    /**
     * 删除当前logicId
     */
    public static void removeLogicId(){
        ContextHolder.removeContext(Constants.LOGIC_ID);
    }


    /**
     * 获取签名后的spanId
     * @param spanId 要签名的spanId
     * @return
     */
    private static String getSignSpanId(String spanId){
        String _spanId= ContextHolder.getContextString(Constants.INVOKE_SPAN_ID);
        String newSpanId= getSign(StringUtils.defaultIfBlank(_spanId,spanId));
        ContextHolder.setContext(Constants.INVOKE_SPAN_ID,newSpanId);
        return newSpanId;
    }

    /**
     * 签名操作
     * @param value
     * @return
     */
    private static String getSign(String value){
        value= StringUtils.defaultIfBlank(value,"0");
        if(!value.contains("."))return value+".1";
        int i=value.lastIndexOf('.')+1;
        return  value.substring(0,i)+Integer.toString(NumberUtils.parseInteger(value.substring(i))+1);
    }

}
