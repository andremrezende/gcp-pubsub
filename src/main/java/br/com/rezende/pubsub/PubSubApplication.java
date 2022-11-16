package br.com.rezende.pubsub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
@PropertySource("classpath:application.properties")
public class PubSubApplication {

	public static void main(String[] args) {
		SpringApplication.run(PubSubApplication.class, args);
	}

}
