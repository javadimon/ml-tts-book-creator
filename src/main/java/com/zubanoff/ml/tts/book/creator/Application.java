package com.zubanoff.ml.tts.book.creator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {

	public static final Object lock = new Object();

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}
