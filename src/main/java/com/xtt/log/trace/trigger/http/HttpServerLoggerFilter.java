package com.xtt.log.trace.trigger.http;

import com.google.common.collect.Maps;
import com.xtt.log.trace.Constants;
import com.xtt.log.trace.MsgType;
import com.xtt.log.trace.helper.TraceHolder;
import com.xtt.log.trace.util.*;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.time.StopWatch;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.util.WebUtils;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
@WebFilter(urlPatterns={"/*"})
public class HttpServerLoggerFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        if (!(request instanceof HttpServletRequest) || !(response instanceof HttpServletResponse)) {
            throw new ServletException("OncePerRequestFilter just supports HTTP requests");
        }

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        if(!httpRequest.getRequestURI().matches("^/(user|open|org|inner)/.*")){
            filterChain.doFilter(request, response);
            return;
        }

        response.setCharacterEncoding("utf-8");
        response.setContentType("application/json; charset=UTF-8");
        HttpServletRequest requestToUse = httpRequest;
        HttpServletResponse responseToUse = httpResponse;
        if (!(request instanceof ContentCachingRequestWrapper)) {
            requestToUse = new ContentCachingRequestWrapper((HttpServletRequest) request);
        }
        if (!(response instanceof ContentCachingResponseWrapper)) {
            responseToUse= new ContentCachingResponseWrapper(httpResponse);
        }
        Throwable ex=null;
        StopWatch stopWatch=new StopWatch();
        try {
            stopWatch.start();
            TraceHolder.setContext(RequestUtils.parseHeader2Span((HttpServletRequest) request));
            filterChain.doFilter(requestToUse, responseToUse);
            stopWatch.stop();
        } catch (Throwable e) {
            stopWatch.stop();
            ex=e;
            try {
                ResponseUtils.output(responseToUse);
            }catch (Exception e1){
                ex=e1;
            }
            throw e;
        }finally {
            responseToUse.addHeader(Constants.X_TOON_TRACE,TraceHolder.getTraceId());
            TraceUtils.trace(MapUtils.putAll(Maps.newLinkedHashMap(),new Object[]{
                    Constants.TRACE_TIME, DateFormatUtils.format(stopWatch.getStartTime()),
                    Constants.MSG_TYPE, "->"+MsgType.http.name(),
                    Constants.CONSUMED_TIME,Long.toString(stopWatch.getTime()),
                    Constants.SOURCE, MapUtils.putAll(Maps.newHashMap(),new Object[]{
                        Constants.URL, httpRequest.getRequestURI(),
                        Constants.CLIENTIP, RequestUtils.getClientIp(httpRequest),
                        Constants.HEADERS, new ServletServerHttpRequest(httpRequest).getHeaders()
                    }),
                    Constants.ARGS, GsonUtils.parseJsonObject(WebUtils.getNativeRequest(requestToUse, ContentCachingRequestWrapper.class).getRequestContent()),
                    Constants.RESULT,GsonUtils.parseJsonObject(WebUtils.getNativeResponse(responseToUse, ContentCachingResponseWrapper.class).getResponseContent())
            }),ex);
            TraceHolder.cleanContext();
        }
    }


    @Override
    public void destroy() {}

}
