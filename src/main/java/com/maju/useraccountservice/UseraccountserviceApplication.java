package com.maju.useraccountservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class UseraccountserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(UseraccountserviceApplication.class, args);
	}

}
