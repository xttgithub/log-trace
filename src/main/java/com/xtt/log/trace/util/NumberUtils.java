package com.xtt.log.trace.util;

import java.util.regex.Pattern;

public class NumberUtils extends org.apache.commons.lang.math.NumberUtils{
    private static final Pattern INT_PATTERN = Pattern.compile("^\\d+$");

    public static boolean isInteger(String str) {
        if (str == null || str.length() == 0)
            return false;
        return INT_PATTERN.matcher(str).matches();
    }

    public static int parseInteger(String str) {
        if (! isInteger(str))
            return 0;
        return Integer.parseInt(str);
    }
}
