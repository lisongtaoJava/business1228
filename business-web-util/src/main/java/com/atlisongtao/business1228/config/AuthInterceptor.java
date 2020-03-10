package com.atlisongtao.business1228.config;

import com.alibaba.fastjson.JSON;
import io.jsonwebtoken.impl.Base64Codec;
import io.jsonwebtoken.impl.Base64UrlCodec;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

// 做拦截器！
@Component
public class AuthInterceptor extends HandlerInterceptorAdapter{
    // 进入控制器之前执行！
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 将token 放入cookie 中！只有在登录的时候，才能获取！
        String token = request.getParameter("newToken");
        if (token!=null){
            CookieUtil.setCookie(request,response,"token",token,WebConst.COOKIE_MAXAGE,false);
        }
        //  当用户登录之后，再访问其他页面的时候， request.getParameter("newToken"); 取不到值了。
        if (token==null){
            token=CookieUtil.getCookieValue(request,"token",false);
        }
        // 当token 不为空的时候，解密取得用户昵称
        if (token!=null){
            // 解密token 取得里面的用户昵称！
            Map map = getUserMapByToken(token);
            // 取得用户的昵称
            String nickName = (String) map.get("nickName");
            // 保存到作用域
            request.setAttribute("nickName",nickName);
        }
        // 在拦截器中获取类上的自定义注解
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        // 获取方法上的注解
        LoginRequire methodAnnotation = handlerMethod.getMethodAnnotation(LoginRequire.class);
        if (methodAnnotation!=null){
            // 做认证调用方法 远程过程调用，httpClient();
            String remoteAddr = request.getHeader("x-forwarded-for");
            String result = HttpClientUtil.doGet(WebConst.VERIFY_ADDRESS + "?token=" + token + "&currentIp=" + remoteAddr);
            // 判断result
            if ("success".equals(result)){
                // 认证成功！做一个保存用户Id
                Map map = getUserMapByToken(token);
                // 获取用户Id
                String userId = (String) map.get("userId");
                // 保存数据
                request.setAttribute("userId",userId);
                return  true;
            }else {
                // 认证失败！需要登录则跳转到登录页面
                if (methodAnnotation.autoRedirect()){
                    // 必须登录 autoRedirect = true; 获取当前的url
                    String requestURL  = request.getRequestURL().toString();
                    String encodeURL = URLEncoder.encode(requestURL, "UTF-8");
                    // 远程调用登录控制器 {}
                    // String url = HttpClientUtil.doGet(WebConst.LOGIN_ADDRESS + "?originUrl=" + encodeURL);
//                    System.out.println(url);
                    // 将页面信息重定向到页面
                    response.sendRedirect(WebConst.LOGIN_ADDRESS + "?originUrl=" + encodeURL);
                    return false;
                }
            }
        }


        return true;
    }
    // 用来解密token，得到map
    private Map getUserMapByToken(String token) {
        // token 三部分组成，用户信息放入私有部分，也就是token 字符串中的第二部分！
        // eyJhbGciOiJIUzI1NiJ9.eyJuaWNrTmFtZSI6IkFkbWluaXN0cmF0b3IiLCJ1c2VySWQiOiIyIn0.WUvbFvXQnTMBGNyHWT-DE41MR9cn7c_W1oAtDAzb7VU
        // eyJuaWNrTmFtZSI6IkFkbWluaXN0cmF0b3IiLCJ1c2VySWQiOiIyIn0 对字符串进行base64 转换
        //  key  salt token
        String tokenUserInfo = StringUtils.substringBetween(token, ".");
        Base64UrlCodec base64UrlCodec = new Base64UrlCodec();
        byte[] decode = base64UrlCodec.decode(tokenUserInfo);
        // decode 转换为字符串
        String userStr = null;
        try {
            userStr = new String(decode,"utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        // 将字符串转换为Map
        Map map = JSON.parseObject(userStr, Map.class);
        return map;
    }

}
