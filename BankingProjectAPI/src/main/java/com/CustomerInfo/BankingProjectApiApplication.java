package com.CustomerInfo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class BankingProjectApiApplication {
	private static final Logger logger = LogManager.getLogger(BankingProjectApiApplication.class);
	public static void main(String[] args) {	
		SpringApplication.run(BankingProjectApiApplication.class, args);
	}
}
