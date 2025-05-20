package com.orakoglu.excelreader;

import java.util.Locale;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableAutoConfiguration
@ComponentScan(basePackages = "com.orakoglu.excelreader")
public class Application {

	public static Locale tr = new Locale("tr", "TR");
	public static Locale en = new Locale("en", "US");

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}
