package org.bremersee.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteDefinition;

@SpringBootApplication
public class Application {

	RouteDefinition rd;

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}
