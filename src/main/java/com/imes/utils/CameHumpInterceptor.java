package com.imes.utils;

import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.plugin.*;

import java.sql.Statement;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author 钟欣凯
 * @version 1.0
 * @Description: MyBatis Map类型大写下划线Key转小写驼峰形式
 * @date 2018年9月17日 11:01:19
 */
@Intercepts(@Signature(type = ResultSetHandler.class, method = "handleResultSets", args = {Statement.class}))
public class CameHumpInterceptor implements Interceptor {

    static Pattern pattern =  Pattern.compile("_[a-z]");

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        //先执行，后处理
        List<Object> list = (List<Object>) invocation.proceed();
        for (Object object : list) {
            if (object instanceof Map) {
                processMap((Map) object);
            } else {
                break;
            }
        }
        return list;
    }

    /**
     * 处理简单对象
     *
     * @param map
     */
    private void processMap(Map map) {
        Map cameHumpMap = new HashMap(16);
        Iterator<Map.Entry> iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry entry = iterator.next();
            String key = (String) entry.getKey();
            String cameHumpKey = underlineToCamelhump(key);
            if (!key.equals(cameHumpKey)) {
                cameHumpMap.put(cameHumpKey, entry.getValue());
                iterator.remove();
            }
        }
        map.putAll(cameHumpMap);
    }

    /**
     * 将下划线风格替换为驼峰风格
     *
     * @param str
     * @return
     */
    public static String underlineToCamelhump(String str) {
        Matcher matcher = pattern.matcher(str);
        StringBuilder builder = new StringBuilder(str);
        for (int i = 0; matcher.find(); i++) {
            builder.replace(matcher.start() - i, matcher.end() - i, matcher.group().substring(1).toUpperCase());
        }
        if (Character.isUpperCase(builder.charAt(0))) {
            builder.replace(0, 1, String.valueOf(Character.toLowerCase(builder.charAt(0))));
        }
        return builder.toString();
    }

    @Override
    public Object plugin(Object target) {
        if (target instanceof ResultSetHandler) {
            return Plugin.wrap(target, this);
        }
        return target;
    }

    @Override
    public void setProperties(Properties properties) {
    }
}