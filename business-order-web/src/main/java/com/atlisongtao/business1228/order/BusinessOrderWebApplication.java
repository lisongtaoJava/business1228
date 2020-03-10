package com.atlisongtao.business1228.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;


@SpringBootApplication
@ComponentScan(basePackages = "com.atlisongtao.business1228")
public class BusinessOrderWebApplication {

	public static void main(String[] args) {
		SpringApplication.run(BusinessOrderWebApplication.class, args);
	}

}
