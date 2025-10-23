package com.example.expensetracker.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RootController {

	@GetMapping("/")
	public String hello() {
		return "Welcome to Expense Tracker Application";
	}
}
