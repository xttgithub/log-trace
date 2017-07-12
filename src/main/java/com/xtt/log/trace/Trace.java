package com.xtt.log.trace;

import com.google.gson.GsonBuilder;
import org.apache.commons.lang3.StringUtils;

/**
 * 核心埋点--跟踪信息
 */
public class Trace {
    /**
     * 调用链ID
     * 调用链的标示，全局唯一
     */
    private String traceId;
    /**
     * 分段ID
     * 同一个调用链下的分段调用ID
     * 对于前端收到请求，生成的spanId固定都是0
     * 签名方式生成:0, 0.1, 0.1.1, 0.2
     */
    private String spanId;
    /**
     * 逻辑ID
     * 同一个分段调用下的业务逻辑ID
     * 标识服务内部的调用顺序
     */
    private String logicId;

    private Trace() {}

    public static Trace of(){
        return new Trace();
    }
    public static Trace of(String traceId){
        return of(traceId,"0");
    }
    public static Trace of(String traceId, String spanId){
        return of(traceId,spanId,"0");
    }
    public static Trace of(String traceId, String spanId,String logicId){
        Trace trace=new Trace();
        trace.setTraceId(traceId);
        trace.setSpanId(spanId);
        trace.setLogicId(logicId);
        return trace;
    }

    public String getTraceId() {
        return traceId;
    }
    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getSpanId() {
        return spanId;
    }
    public void setSpanId(String spanId) {
        this.spanId = spanId;
    }

    public String getLogicId() {
        return logicId;
    }

    public void setLogicId(String logicId) {
        this.logicId = logicId;
    }

    @Override
    public String toString() {
        return (new GsonBuilder()).create().toJson(this);
    }
}
