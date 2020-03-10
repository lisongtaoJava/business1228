package com.atlisongtao.business1228.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/*
    每次走的请求实际都是一个@RequestMapping(); 基于方法级别！

*/
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LoginRequire {
    // 自定义一个字段
    boolean autoRedirect() default true;
}
