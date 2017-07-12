package com.xtt.log.trace.util;

import com.google.common.collect.Maps;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.lang3.StringUtils;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLDecoder;
import java.util.Map;
import java.util.Set;

public class GsonUtils {
    private static Gson gson;
    static{
        gson = new GsonBuilder()
                .disableHtmlEscaping()//解决一些字符自动转换为Unicode转义字符
                .setDateFormat("yyyy-MM-dd HH:mm:ss")
                .registerTypeAdapter(//解决字符串解析成小数问题
                        new TypeToken<Map<String, Object>>(){}.getType(),
                        new JsonDeserializer<Map<String, Object>>() {
                            @Override
                            public Map<String, Object> deserialize(JsonElement json, Type typeOfT,
                               JsonDeserializationContext context) throws JsonParseException {
                                Map<String,Object> map= Maps.newLinkedHashMap();
                                JsonObject jsonObject = json.getAsJsonObject();
                                Set<Map.Entry<String, JsonElement>> entrySet = jsonObject.entrySet();
                                for (Map.Entry<String, JsonElement> entry : entrySet) {
                                    map.put(entry.getKey(), entry.getValue());
                                }
                                return map;
                            }
                        })
                .create();
    }
    public static String toJson(Object src){
        return gson.toJson(src);
    }

    public static Map<String,Object> fromJson(String src){
        return gson.fromJson(src,new TypeToken<Map<String, Object>>(){}.getType());
    }
    public static Object parseJsonObject(String src){
        if(StringUtils.isBlank(src))return src;
        try {
            return gson.fromJson(src, JsonObject.class);
        }catch (Exception e){
            return src;
        }
    }
}
