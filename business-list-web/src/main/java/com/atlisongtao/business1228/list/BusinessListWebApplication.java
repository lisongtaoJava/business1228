package com.atlisongtao.business1228.list;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.atlisongtao.business1228")
public class BusinessListWebApplication {

	public static void main(String[] args) {
		SpringApplication.run(BusinessListWebApplication.class, args);
	}

}
