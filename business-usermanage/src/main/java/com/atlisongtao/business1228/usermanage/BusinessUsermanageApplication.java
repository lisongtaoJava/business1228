package com.atlisongtao.business1228.usermanage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan(basePackages = "com.atlisongtao.business1228.usermanage.mapper")
@ComponentScan(basePackages = "com.atlisongtao.business1228")
public class BusinessUsermanageApplication {

	public static void main(String[] args) {
		SpringApplication.run(BusinessUsermanageApplication.class, args);
	}

}
