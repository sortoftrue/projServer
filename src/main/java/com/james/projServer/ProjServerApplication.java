package com.james.projServer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
public class ProjServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProjServerApplication.class, args);
	}

	@Bean
	public WebMvcConfigurer corsConfigurer(){
		return new EnableCors("/**","*");
	}

}
