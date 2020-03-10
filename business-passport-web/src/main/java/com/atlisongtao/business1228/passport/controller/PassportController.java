package com.atlisongtao.business1228.passport.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atlisongtao.business1228.bean.UserInfo;

import com.atlisongtao.business1228.passport.config.JwtUtil;
import com.atlisongtao.business1228.service.UserInfoService;
import org.omg.CORBA.StringHolder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PassportController {

    @Reference
    private UserInfoService userInfoService;

    @Value("${token.key}")
    private String key;

    @RequestMapping("index")
    public String index(HttpServletRequest request){
        String originUrl = request.getParameter("originUrl");

        request.setAttribute("originUrl",originUrl);

        return "index";
    }

    @RequestMapping("login")
    @ResponseBody
    public String login(UserInfo userInfo,HttpServletRequest request){
        String salt = request.getHeader("X-forwarded-for"); // 192.168.192.1
        // 判断
        if (userInfo!=null){
            UserInfo info = userInfoService.login(userInfo);
            if (info!=null){
                // 制作token！
                HashMap<String, Object> map = new HashMap<>();
                map.put("userId",info.getId());
                map.put("nickName",info.getNickName());
                String token = JwtUtil.encode(key, map, salt);
                System.out.println("newToken:"+token);
                return token; // token
            }else {
                return "fail";
            }
        }
        return "fail";
    }

    // 用户认证！在控制器verify?token=xxx&currentIp=salt
    @RequestMapping("verify")
    @ResponseBody
    public String verify(HttpServletRequest request){
        // 认证需要得到 服务器ip 地址  salt，还需要得到 token
        String token = request.getParameter("token");
        String currentIp = request.getParameter("currentIp");
        // 准备解密操作
        Map<String, Object> map = JwtUtil.decode(token, key, currentIp);
        if (map!=null && map.size()>0){
            // 利用String  userId = map.get("userId")
            String userId = (String) map.get("userId");
            //  跟redis 做比较
            UserInfo userInfo = userInfoService.verify(userId);
            if (userInfo!=null){
                return "success";
            }else {
                return "fail";
            }
        }
        return "fail";

    }
}
