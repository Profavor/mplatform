package com.favorsoft.mplatform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class MplatformApplication {

	public static void main(String[] args) {
		SpringApplication.run(MplatformApplication.class, args);
	}

}
