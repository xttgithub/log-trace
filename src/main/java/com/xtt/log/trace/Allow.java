package com.xtt.log.trace;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 标记可以输出跟踪日志
 */
@Target({METHOD,TYPE})
@Retention(RUNTIME)
public @interface Allow {
}
