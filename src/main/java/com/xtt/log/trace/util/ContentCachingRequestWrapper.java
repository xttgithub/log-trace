package com.xtt.log.trace.util;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpMethod;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.*;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Contact
 *
 * @author liqiang
 * @date 2016-11-24
 */
public class ContentCachingRequestWrapper extends HttpServletRequestWrapper {
    private static final String JSON_CONTENT_TYPE = "application/json";
    private ByteArrayOutputStream cachedBytes;

    public ContentCachingRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        if (cachedBytes == null)
            cacheInputStream();

        return new CachedServletInputStream();
    }

    @Override
    public BufferedReader getReader() throws IOException{
        return new BufferedReader(new InputStreamReader(getInputStream()));
    }

    private void cacheInputStream() throws IOException {
        cachedBytes = new ByteArrayOutputStream();
        IOUtils.copy(super.getInputStream(), cachedBytes);
    }

    public class CachedServletInputStream extends ServletInputStream {
        private ByteArrayInputStream input;

        public CachedServletInputStream() {
            input = new ByteArrayInputStream(cachedBytes.toByteArray());
        }

        @Override
        public boolean isFinished() {
            return false;
        }

        @Override
        public boolean isReady() {
            return false;
        }

        @Override
        public void setReadListener(ReadListener readListener) {

        }

        @Override
        public int read() throws IOException {
            return input.read();
        }
    }
    public  String getRequestContent() {
        try {
            String requestEncoding = getCharacterEncoding();
            String contentType = getContentType();

            if(HttpMethod.POST.name().matches(getMethod())&& contentType != null && contentType.contains(JSON_CONTENT_TYPE)){
                return IOUtils.toString(getInputStream(),requestEncoding);
            }

            StringBuilder msg = new StringBuilder();
            Map<String, String[]> form = super.getParameterMap();
            for (Iterator<String> nameIterator = form.keySet().iterator(); nameIterator.hasNext(); ) {
                String name = nameIterator.next();
                List<String> values = Arrays.asList(form.get(name));
                for (Iterator<String> valueIterator = values.iterator(); valueIterator.hasNext(); ) {
                    String value = valueIterator.next();
                    msg.append(URLEncoder.encode(name, requestEncoding));
                    if (value != null) {
                        msg.append('=');
                        msg.append(URLEncoder.encode(value, requestEncoding));
                        if (valueIterator.hasNext()) {
                            msg.append('&');
                        }
                    }
                }
                if (nameIterator.hasNext()) {
                    msg.append('&');
                }
            }
            return StringUtils.isNotBlank(msg.toString())?msg.toString():"";
        }catch (IOException ex) {
            throw new IllegalStateException("Failed to write request parameters to cached content", ex);
        }
    }
}