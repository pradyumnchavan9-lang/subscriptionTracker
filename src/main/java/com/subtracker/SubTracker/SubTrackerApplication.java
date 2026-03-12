package com.subtracker.SubTracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class SubTrackerApplication {

	public static void main(String[] args) {
		SpringApplication.run(SubTrackerApplication.class, args);
	}

}
