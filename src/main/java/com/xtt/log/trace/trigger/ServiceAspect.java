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
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.AnnotationUtils;

@Aspect
public class ServiceAspect {
    @Around("(@within(org.springframework.stereotype.Service)&&execution(public * *(..)))"
            + "||@annotation(org.springframework.stereotype.Service)")
    public Object serviceClassLog(ProceedingJoinPoint pjp) throws Throwable {
        if(AnnotationUtils.findAnnotation(((MethodSignature)pjp.getSignature()).getMethod(),Ignore.class)!=null){
            return pjp.proceed();
        }

        StopWatch stopWatch=new StopWatch();
        Object ret=null;
        Throwable ex=null;
        try {
            stopWatch.start();
            TraceHolder.incrLogicId();
            ret = pjp.proceed();
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
                    Constants.LOCATION, ImmutableMap.of(
                        Constants.CLASS,pjp.getTarget().getClass().getName(),
                        Constants.METHOD,pjp.getSignature().getName()),
                    Constants.ARGS, pjp.getArgs(),
                    Constants.RESULT,ret
            }),ex);
            TraceHolder.removeLogicId();
        }
    }
}
