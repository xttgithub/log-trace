package com.xtt.log.trace.trigger.kafka;


import kafka.producer.KeyedMessage;

import java.util.List;
import java.util.Map;

public interface IKafkaMessageFilter {
	void preSend(Map<String,Object> context, Map<String, String> header);
	void afterSend(Map<String,Object> context, List<KeyedMessage<String, String>> messages,Throwable e);

	void preReceive(Map<String,Object> context,Map<String, String> header);
	void afterReceive(Map<String,Object> context, Map<String, String> header,String message,Throwable e);

}
