package crio.vicara;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
public class VicaraT7Application {

	public static void main(String[] args) {
		SpringApplication.run(VicaraT7Application.class, args);
	}

	@Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurer() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				registry.addMapping("/api/**")
						.allowedOrigins("*")
						.allowedHeaders("GET", "POST", "PUT", "DELETE", "PATCH")
						.allowCredentials(true)
						.allowedHeaders("*");
			}
		};
	}
}
