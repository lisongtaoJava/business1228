package com.atlisongtao.business1228.cart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.atlisongtao.business1228")
public class BusinessCartWebApplication {

	public static void main(String[] args) {
		SpringApplication.run(BusinessCartWebApplication.class, args);
	}

}
