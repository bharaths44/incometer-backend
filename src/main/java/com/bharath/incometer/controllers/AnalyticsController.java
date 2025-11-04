package com.bharath.incometer.controllers;

import com.bharath.incometer.models.BudgetAnalytics;
import com.bharath.incometer.models.CategoryAnalytics;
import com.bharath.incometer.models.ExpenseSummary;
import com.bharath.incometer.service.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

	private final AnalyticsService analyticsService;

	public AnalyticsController(AnalyticsService analyticsService) {
		this.analyticsService = analyticsService;
	}

	@GetMapping("/user/{userId}/expense-summary")
	public ResponseEntity<ExpenseSummary> getExpenseSummary(
		@PathVariable UUID userId) {
		ExpenseSummary summary = analyticsService.getExpenseSummary(userId);
		return ResponseEntity.ok(summary);
	}

	@GetMapping("/user/{userId}/categories")
	public ResponseEntity<List<CategoryAnalytics>> getCategoryAnalytics(
		@PathVariable UUID userId) {
		List<CategoryAnalytics> categories = analyticsService.getCategoryAnalytics(userId);
		return ResponseEntity.ok(categories);
	}

	@GetMapping("/user/{userId}/budgets")
	public ResponseEntity<List<BudgetAnalytics>> getBudgetAnalytics(
		@PathVariable UUID userId) {
		List<BudgetAnalytics> budgets = analyticsService.getBudgetAnalytics(userId);
		return ResponseEntity.ok(budgets);
	}
}
