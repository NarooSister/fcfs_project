package com.sparta.fcfsproject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class FcfsprojectApplication {

	public static void main(String[] args) {
		SpringApplication.run(FcfsprojectApplication.class, args);
	}

}
