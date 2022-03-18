package com.nowcoder.community;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class CommunityApplication {

	public static void main(String[] args) {
		ConfigurableApplicationContext run = SpringApplication.run(CommunityApplication.class, args);
	}

}
