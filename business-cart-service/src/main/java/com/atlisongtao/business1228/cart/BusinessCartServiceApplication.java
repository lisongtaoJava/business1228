package com.atlisongtao.business1228.cart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.atlisongtao.business1228")
@MapperScan(basePackages = "com.atlisongtao.business1228.cart.mapper")
public class BusinessCartServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(BusinessCartServiceApplication.class, args);
	}

}
