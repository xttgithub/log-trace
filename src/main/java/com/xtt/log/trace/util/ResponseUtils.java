package com.xtt.log.trace.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Serializable;

public class ResponseUtils {

    public static  void output(HttpServletResponse response) throws IOException {
        FacadeResult headerError =new FacadeResult(100000,"error");
        response.getWriter().append(headerError.toString());
    }
    public static  class FacadeResult implements Serializable {
        private static final long serialVersionUID = 3114794797668742669L;
        private static final Gson gson = new GsonBuilder().disableHtmlEscaping().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
        private Meta meta;
        private Object data;

        public FacadeResult() {
        }

        public FacadeResult(int code, String message) {
            this.meta = new Meta(code, message);
        }


        @Override
        public String toString() {
            return this.toJsonString(null);
        }

        public String toString(String msg) {
            return this.toJsonString(msg);
        }

        protected String toJsonString(String msg) {
            return gson.toJson(this).toString();
        }

        public Meta getMeta() {
            return meta;
        }

        public void setMeta(Meta meta) {
            this.meta = meta;
        }

        public Object getData() {
            return data;
        }

        public void setData(Object data) {
            this.data = data;
        }

    }
    public static class Meta {
        private Integer code;
        private String message;

        public Meta() {
            super();
        }
        public Meta(Integer code, String message) {
            super();
            this.code = code;
            this.message = message;
        }

        public Integer getCode() {
            return code;
        }

        public void setCode(Integer code) {
            this.code = code;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }
}
