package com.xtt.log.trace.trigger;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.xtt.log.trace.Constants;
import com.xtt.log.trace.Ignore;
import com.xtt.log.trace.MsgType;
import com.xtt.log.trace.helper.TraceHolder;
import com.xtt.log.trace.util.DateFormatUtils;
import com.xtt.log.trace.util.TraceUtils;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.time.StopWatch;
import org.springframework.core.annotation.AnnotationUtils;

public class ServiceInterceptor implements MethodInterceptor{
    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        if(AnnotationUtils.findAnnotation(methodInvocation.getMethod(),Ignore.class)!=null){
            return methodInvocation.proceed();
        }

        StopWatch stopWatch=new StopWatch();
        Object ret=null;
        Throwable ex=null;
        try {
            stopWatch.start();
            TraceHolder.incrLogicId();
            ret = methodInvocation.proceed();
            stopWatch.stop();
            return ret;
        } catch (Throwable e) {
            stopWatch.stop();
            ex=e;
            throw e;
        }finally {
            TraceUtils.trace(MapUtils.putAll(Maps.newLinkedHashMap(),new Object[]{
                    Constants.TRACE_TIME, DateFormatUtils.format(stopWatch.getStartTime()),
                    Constants.MSG_TYPE, MsgType.service.name(),
                    Constants.CONSUMED_TIME,Long.toString(stopWatch.getTime()),
                    Constants.LOCATION,ImmutableMap.of(
                        Constants.CLASS,methodInvocation.getThis().getClass().getName(),
                        Constants.METHOD,methodInvocation.getMethod().getName()
                    ),
                    Constants.ARGS, methodInvocation.getArguments(),
                    Constants.RESULT,ret
            }),ex);
            TraceHolder.removeLogicId();
        }
    }
}
