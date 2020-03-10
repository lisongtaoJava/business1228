package com.atlisongtao.business1228.manage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan(basePackages = "com.atlisongtao.business1228.manage.mapper")
@ComponentScan(basePackages = "com.atlisongtao.business1228")
public class BusinessManageServiceApplication {


	public static void main(String[] args) {
		SpringApplication.run(BusinessManageServiceApplication.class, args);
	}

}
