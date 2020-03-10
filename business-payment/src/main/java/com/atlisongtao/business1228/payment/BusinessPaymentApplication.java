package com.atlisongtao.business1228.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.atlisongtao.business1228")
@MapperScan(basePackages = "com.atlisongtao.business1228.payment.mapper")
public class BusinessPaymentApplication {

	public static void main(String[] args) {
		SpringApplication.run(BusinessPaymentApplication.class, args);
	}

}
