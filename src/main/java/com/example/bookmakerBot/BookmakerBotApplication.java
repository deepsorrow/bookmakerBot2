package com.example.bookmakerBot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BookmakerBotApplication implements CommandLineRunner {

	@Autowired
	BrowserProfile browserProfile;

	public static void main(String[] args) {
		SpringApplication.run(BookmakerBotApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		browserProfile.run(new String[]{});
	}
}
