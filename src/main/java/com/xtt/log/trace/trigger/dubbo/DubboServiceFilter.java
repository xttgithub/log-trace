package com.xtt.log.trace.trigger.dubbo;

import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.*;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.xtt.log.trace.Constants;
import com.xtt.log.trace.MsgType;
import com.xtt.log.trace.Trace;
import com.xtt.log.trace.helper.TraceHolder;
import com.xtt.log.trace.util.DateFormatUtils;
import com.xtt.log.trace.util.TraceUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.time.StopWatch;

@Activate(group = com.alibaba.dubbo.common.Constants.PROVIDER,
		value = {com.alibaba.dubbo.common.Constants.MONITOR_KEY,
				com.alibaba.dubbo.common.Constants.ASYNC_KEY})
public class DubboServiceFilter implements Filter {
	@Override
	public Result invoke(Invoker<?> invoker, Invocation invocation)throws RpcException {
		Result result=null;
		Throwable ex=null;
		StopWatch stopWatch=new StopWatch();
		try {
			stopWatch.start();
			TraceHolder.setContext(Trace.of(MapUtils.getString(invocation.getAttachments(),Constants.TRACE_ID),
					MapUtils.getString(invocation.getAttachments(),Constants.SPAN_ID)));
			result=invoker.invoke(invocation);
			stopWatch.stop();
			return result;
		} catch (Exception e) {
			stopWatch.stop();
			ex=e;
			throw e;
		}finally {
			TraceUtils.trace(MapUtils.putAll(Maps.newLinkedHashMap(),new Object[]{
					Constants.TRACE_TIME, DateFormatUtils.format(stopWatch.getStartTime()),
					Constants.MSG_TYPE, "->"+MsgType.dubbo.name(),
					Constants.CONSUMED_TIME,Long.toString(stopWatch.getTime()),
					Constants.SOURCE,ImmutableMap.of(
						Constants.CLIENTIP, RpcContext.getContext().getRemoteAddress().toString()
					),
					Constants.LOCATION,ImmutableMap.of(
						Constants.CLASS,invoker.getUrl().getPath(),
						Constants.METHOD,invocation.getMethodName(),
						Constants.SERVERIP,invoker.getUrl().getHost()
					),
					Constants.ARGS, invocation.getArguments(),
					Constants.RESULT,result!=null?result.getValue():null
			}),result!=null&&result.hasException()?result.getException():ex);
			TraceHolder.cleanContext();
		}
	}
}
