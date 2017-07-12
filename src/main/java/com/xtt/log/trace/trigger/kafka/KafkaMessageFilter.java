package com.xtt.log.trace.trigger.kafka;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.xtt.log.trace.Constants;
import com.xtt.log.trace.MsgType;
import com.xtt.log.trace.Trace;
import com.xtt.log.trace.helper.TraceHolder;
import com.xtt.log.trace.util.DateFormatUtils;
import com.xtt.log.trace.util.RequestUtils;
import com.xtt.log.trace.util.TraceUtils;
import kafka.producer.KeyedMessage;
import org.apache.commons.collections.MapUtils;

import java.util.List;
import java.util.Map;

public class KafkaMessageFilter implements IKafkaMessageFilter {

    @Override
    public void preSend(Map<String, Object> context,Map<String, String> header) {
        Trace trace= TraceHolder.getSendingTrace();
        header.put(Constants.TRACE_ID, trace.getTraceId());
        header.put(Constants.SPAN_ID, trace.getSpanId());
        header.put(Constants.CLIENTIP, RequestUtils.getHostIp());
    }

    @Override
    public void afterSend(Map<String, Object> context, List<KeyedMessage<String, String>> messages, Throwable e) {
        TraceHolder.incrLogicId();
        TraceUtils.trace(MapUtils.putAll(Maps.newLinkedHashMap(),new Object[]{
                Constants.TRACE_TIME, DateFormatUtils.format(MapUtils.getLong(context,"start")),
                Constants.MSG_TYPE, MsgType.kafka.name()+"->",
                Constants.CONSUMED_TIME,MapUtils.getString(context,"costTime"),
                Constants.LOCATION,ImmutableMap.of(
                    Constants.TOPIC,MapUtils.getString(context,"topic")
                ),
                Constants.ARGS, messages,
                Constants.RESULT,String.format("{\"code\":%d}",e!=null?0:1)
        }),e);
        TraceHolder.removeLogicId();
    }

    @Override
    public void preReceive(Map<String, Object> context,Map<String, String> header) {
        TraceHolder.setContext(Trace.of(MapUtils.getString(header,Constants.TRACE_ID),
                MapUtils.getString(header,Constants.SPAN_ID)));
    }

    @Override
    public void afterReceive(Map<String, Object> context, Map<String, String> header,String message, Throwable e) {
        TraceUtils.trace(MapUtils.putAll(Maps.newLinkedHashMap(),new Object[]{
                Constants.TRACE_TIME, DateFormatUtils.format(MapUtils.getLong(context,"start")),
                Constants.MSG_TYPE, "->"+MsgType.kafka.name(),
                Constants.CONSUMED_TIME,MapUtils.getString(context,"costTime"),
                Constants.SOURCE,ImmutableMap.of(
                    Constants.CLIENTIP, MapUtils.getString(header,Constants.CLIENTIP)
                ),
                Constants.LOCATION,ImmutableMap.of(
                    Constants.TOPIC,MapUtils.getString(context,"topic")
                ),
                Constants.ARGS,null,
                Constants.RESULT,message
        }),e);
        TraceHolder.cleanContext();
    }
}
