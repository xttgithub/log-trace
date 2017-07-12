package com.xtt.log.trace.helper;


import com.xtt.log.trace.Constants;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.NDC;
import org.slf4j.MDC;

import java.util.Map;

/**
 * 上下文操作
 * 支持同步操作
 * TODO..支持异步操作
 */
public class ContextHolder {
    /**
     * 获取上下文
     * @return
     */
    public static Map<String, String> getContextMap(){
        return MDC.getCopyOfContextMap();
    }

    /**
     * 设置上下文
     * @param key
     * @param value
     */
    public static void setContext(String key,String value){
        if(StringUtils.equals(key, Constants.LOGIC_ID)){
            NDC.push(value);
        }else{
            MDC.put(key,value);
        }
    }

    /**
     * 自增
     */
    public static  void incrContext(String key) {
        String oldValue=getContextString(key);
        String newValue=StringUtils.isBlank(oldValue)?"0":String.valueOf(Integer.parseInt(oldValue)+1);
        setContext(key,newValue);
    }

    /**
     * 从上下文中获取指定值
     * @param key
     * @return
     */
    public static  String getContextString(String key){
        if(StringUtils.equals(key,Constants.LOGIC_ID))return  NDC.peek();
        return MapUtils.getString(getContextMap(),key,"");
    }


    /**
     * 删除特定的key
     */
    public static void removeContext(String key) {
        if(StringUtils.equals(key,Constants.LOGIC_ID)){
            NDC.pop();
        }else{
            MDC.remove(key);
        }
    }
    /**
     * 清除上下文
     */
    public static void clearContext() {
        NDC.remove();
        MDC.clear();
    }

}
