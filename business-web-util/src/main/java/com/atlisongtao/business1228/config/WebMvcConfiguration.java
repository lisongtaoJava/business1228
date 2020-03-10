package com.atlisongtao.business1228.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
// 配置拦截器！
/*
    springmvc.xml
    <interceptors>
        <interceptor>
            <bean class="com.atguigu.gmall0715.config.AuthInterceptor" >
            <mvc:mapping path="/**"/>
        </interceptor>
    </interceptor>
 */
@Configuration
public class WebMvcConfiguration extends WebMvcConfigurerAdapter {

    @Autowired
    private AuthInterceptor authInterceptor;

    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor).addPathPatterns("/**");
        super.addInterceptors(registry);
    }

}
