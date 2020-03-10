package com.atlisongtao.business1228.passport;




import com.atlisongtao.business1228.passport.config.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;

@SpringBootTest
class BusinessPassportWebApplicationTests {

	@Test
	void contextLoads() {
	}

	@Test
	public void testJWT(){
		String key ="ATBUSINESS";
		String slat = "192.168.192.1";
		HashMap<String,Object> map = new HashMap<>();
		map.put("userId",1001);
		map.put("nickName","Administrator");
		String token = JwtUtil.encode(key,map,slat);
		System.out.println("token:="+token);

		Map<String,Object> objectMap = JwtUtil.decode(token,key,"192.168.192.128");
		System.out.println(objectMap);
	}

}
