package com.xtt.log.trace.trigger.http;


import com.google.common.collect.Maps;
import com.xtt.log.trace.Constants;
import com.xtt.log.trace.MsgType;
import com.xtt.log.trace.Trace;
import com.xtt.log.trace.helper.TraceHolder;
import com.xtt.log.trace.util.DateFormatUtils;
import com.xtt.log.trace.util.GsonUtils;
import com.xtt.log.trace.util.TraceUtils;
import okhttp3.*;
import okhttp3.internal.Util;
import okhttp3.internal.http.HttpHeaders;
import okio.Buffer;
import okio.BufferedSource;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.StopWatch;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.Arrays;

public class HttpClientLoggerInterceptor implements Interceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Response response=null;
        Throwable ex=null;
        StopWatch stopWatch=new StopWatch();
        try {
            stopWatch.start();
            TraceHolder.incrLogicId();
            Trace trace= TraceHolder.getSendingTrace();
            if(StringUtils.isNotBlank(trace.getTraceId())&&StringUtils.isNotBlank(trace.getSpanId())) {
                request = request.newBuilder()
                        .addHeader(Constants.X_TOON_TRACE, trace.getTraceId())
                        .addHeader(Constants.X_TOON_SPAN, trace.getSpanId())
                        .build();
            }
            response=chain.proceed(request);
            stopWatch.stop();
            return response;
        }catch (Exception e){
            stopWatch.stop();
            ex=e;
            throw e;
        }finally {
            TraceUtils.trace(MapUtils.putAll(Maps.newLinkedHashMap(),new Object[]{
                    Constants.TRACE_TIME, DateFormatUtils.format(stopWatch.getStartTime()),
                    Constants.MSG_TYPE, MsgType.http.name()+"->",
                    Constants.CONSUMED_TIME,Long.toString(stopWatch.getTime()),
                    Constants.SOURCE, MapUtils.putAll(Maps.newHashMap(),new Object[]{
                        Constants.URL, request.url().uri(),
                        Constants.PROTOCOL,response!=null ?response.protocol().toString():null,
                        Constants.SERVERIP,request.url().host(),
                        Constants.HEADERS, request.headers()
                    }),
                    Constants.ARGS, GsonUtils.parseJsonObject(getRequestBody(request)),
                    Constants.RESULT,GsonUtils.parseJsonObject(getResponseContent(response))
            }),ex);
            TraceHolder.removeLogicId();
        }
    }
    private static String getRequestBody(Request request) {
        RequestBody requestBody=request.body();
        if(requestBody!=null) {
            try {
                final Buffer buffer = new Buffer();
                requestBody.writeTo(buffer);
                //
                BufferedInputStream bufferedInputStream = new BufferedInputStream(buffer.inputStream());
                final StringBuffer resultBuffer = new StringBuffer();
                byte[] inputBytes = new byte[1024];
                while (true) {
                    int count = bufferedInputStream.read(inputBytes);
                    if (count <= 0) {
                        break;
                    }
                    resultBuffer.append(new String(Arrays.copyOf(inputBytes, count), Util.UTF_8));
                }
                final String parameter = URLDecoder.decode(resultBuffer.toString(),
                        Util.UTF_8.name());
                bufferedInputStream.close();


                return parameter;
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }
    private static String getResponseContent(Response response) throws IOException {
        if(response!=null && HttpHeaders.hasBody(response)){
            ResponseBody responseBody = response.body();
            BufferedSource source = responseBody.source();
            source.request(Long.MAX_VALUE); // Buffer the entire body.
            Buffer buffer = source.buffer();
            return buffer.clone().readString(Charset.forName("UTF-8"));
        }
        return null;
    }
}
