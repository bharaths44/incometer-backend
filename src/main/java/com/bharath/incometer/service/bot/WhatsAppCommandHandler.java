package com.bharath.incometer.service.bot;

import com.bharath.incometer.entities.Users;
import com.bharath.incometer.models.WhatsAppMessageRequest;
import com.bharath.incometer.service.TransactionService;
import com.bharath.incometer.service.TwilioService;
import com.bharath.incometer.utils.MessageUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Component
public class WhatsAppCommandHandler {

	private final TwilioService twilioService;
	private final TransactionService transactionService;
	private final MessageUtils messageUtils;
	private final TransactionMessageHandler transactionHandler;

	@Value("${app.registration-url}")
	private String registrationUrl;

	private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy");

	public WhatsAppCommandHandler(TwilioService twilioService, TransactionService transactionService,
	                              MessageUtils messageUtils, TransactionMessageHandler transactionHandler) {
		this.twilioService = twilioService;
		this.transactionService = transactionService;
		this.messageUtils = messageUtils;
		this.transactionHandler = transactionHandler;
	}

	public void handleUnregisteredUser(String from) {
		String message =
			"❌ You are not registered with Incometer yet.\n" + "Register here: " + registrationUrl + "\n\n" +
			"After registering, send a message like: \"spent 150 on groceries\"";
		twilioService.sendWhatsAppMessage(from, message);
	}

	public void handleGreetings(String from) {
		String welcome = """
			👋 Hi there!
			Welcome back to *Incometer* — your quick expense tracker.
			
			You can:
			• Add an expense: "spent 200 on food with cash"
			• Add income: "received 5000 salary"
			• Ask for reports: "summary monthly" or "balance"
			
			Type *help* to see all commands.
			""";
		twilioService.sendWhatsAppMessage(from, welcome);
	}

	public void handleHelp(String from) {
		String help = """
			📘 *Incometer Help*
			
			*➕ Add Transactions*
			• spent 120 on groceries with cash
			• received 5000 salary
			
			*📊 Reports & Analytics*
			• summary daily/weekly/monthly
			• balance
			• category summary daily/weekly/monthly
			
			*📋 View Data*
			• list transactions
			
			💡 _Quick tip: Keep it simple!_
			_"spent 120 groceries" works too_
			""";
		twilioService.sendWhatsAppMessage(from, help);
	}

	public void handleSummary(String from, String body, Users user) {
		String[] parts = body.split("\\s+");
		if (parts.length < 2) {
			twilioService.sendWhatsAppMessage(from,
			                                  "⚠️ Please specify a period:\n*summary daily* | *summary weekly* | " +
			                                  "*summary monthly*");
			return;
		}
		String period = parts[1].toLowerCase();
		LocalDate start;
		LocalDate end = LocalDate.now();
		String periodName;
		switch (period) {
			case "daily" -> {
				start = end;
				periodName = "Daily";
			}
			case "weekly" -> {
				start = end.minusDays(6); // last 7 days including today
				periodName = "Weekly";
			}
			case "monthly" -> {
				start = end.withDayOfMonth(1);
				periodName = "Monthly";
			}
			default -> {
				twilioService.sendWhatsAppMessage(from, "❌ Invalid period. Use: *daily*, *weekly*, or *monthly*");
				return;
			}
		}

		try {
			Map<String, BigDecimal> summary = transactionService.getSummaryByPeriod(user.getUserId(), start, end);
			String incomeStr = messageUtils.formatCurrency(summary.get("income"));
			String expenseStr = messageUtils.formatCurrency(summary.get("expense"));
			String netStr = messageUtils.formatCurrency(messageUtils.calculateNet(summary.get("income"),
			                                                                      summary.get("expense")));

			String message = String.format(
				"📊 *%s Summary*\n%s - %s\n\n💰 Income: *%s*\n💸 Expense: *%s*\n━━━━━━━━━━━\n📈 Net: *%s*",
				periodName,
				start.format(DATE_FMT),
				end.format(DATE_FMT),
				incomeStr,
				expenseStr,
				netStr);

			twilioService.sendWhatsAppMessage(from, message);
		} catch (Exception e) {
			twilioService.sendWhatsAppMessage(from,
			                                  "❌ Couldn't retrieve your summary right now. Please try again later.");
		}
	}

	public void handleBalance(String from, Users user) {
		try {
			BigDecimal balance = transactionService.getBalance(user.getUserId());
			String message = "💰 Current balance: " + messageUtils.formatCurrency(balance);
			twilioService.sendWhatsAppMessage(from, message);
		} catch (Exception e) {
			twilioService.sendWhatsAppMessage(from,
			                                  "❌ Couldn't fetch your balance right now. Please try again later" + ".");
		}
	}

	public void handleListTransactions(String from, Users user) {
		try {
			List<?> transactions = transactionService.getRecentTransactions(user.getUserId(), 10);

			if (transactions == null || transactions.isEmpty()) {
				twilioService.sendWhatsAppMessage(from, "_No recent transactions found._");
				return;
			}

			StringBuilder sb = new StringBuilder("""
				                                     🧾 *Recent Transactions*\
				                                     
				                                     (ID | Date | Amount | Category)\
				                                     
				                                     
				                                     """);

			for (Object obj : transactions) {
				try {
					var cls = obj.getClass();
					var id = cls.getMethod("transactionId").invoke(obj);
					var date = cls.getMethod("transactionDate").invoke(obj);
					var amount = cls.getMethod("amount").invoke(obj);
					var category = cls.getMethod("category").invoke(obj);

					String categoryName =
						category == null ? "—" : category.getClass().getMethod("name").invoke(category).toString();
					String formattedAmount = messageUtils.formatCurrency(amount);
					String dateStr =
						date == null ? "—" : ((LocalDate) date).format(DateTimeFormatter.ofPattern("dd/MM"));

					sb.append(String.format("️-```%s``` | %s | *%s* | _%s_\n",
					                        id,
					                        dateStr,
					                        formattedAmount,
					                        categoryName));

				} catch (Exception e) {
					sb.append("▪️ ").append(obj).append("\n");
				}
			}

			twilioService.sendWhatsAppMessage(from, sb.toString());

		} catch (Exception e) {
			twilioService.sendWhatsAppMessage(from, "❌ Error retrieving recent transactions.");
		}
	}

	public void handleCategorySummary(String from, String body, Users user) {
		String[] parts = body.split("\\s+");
		LocalDate start = null;
		LocalDate end = LocalDate.now();
		String periodName = "All Time";

		if (parts.length >= 3) {
			String period = parts[2].toLowerCase();
			switch (period) {
				case "daily" -> {
					start = end;
					periodName = "Daily";
				}
				case "weekly" -> {
					start = end.minusDays(6);
					periodName = "Weekly";
				}
				case "monthly" -> {
					start = end.withDayOfMonth(1);
					periodName = "Monthly";
				}
				default -> {
					twilioService.sendWhatsAppMessage(from,
					                                  "❌ Invalid period for category summary. Use: *daily*, *weekly*, " +
					                                  "or *monthly*");
					return;
				}
			}
		}

		try {
			Map<String, BigDecimal> catSum;
			if (start != null) {
				catSum = transactionService.getCategorySummaryByPeriod(user.getUserId(), start, end);
			} else {
				catSum = transactionService.getCategorySummary(user.getUserId());
			}

			if (catSum == null || catSum.isEmpty()) {
				String msg = start != null
				             ? "_No category data for this period._"
				             : "_No category data available yet. Start adding transactions!_";
				twilioService.sendWhatsAppMessage(from, msg);
				return;
			}

			StringBuilder sb = new StringBuilder(String.format("📊 *Category Summary (%s)*\n\n", periodName));
			for (Map.Entry<String, BigDecimal> entry : catSum.entrySet()) {
				sb.append(String.format("▪ %s: *%s*\n", entry.getKey(),
				                        messageUtils.formatCurrency(entry.getValue())));
			}
			twilioService.sendWhatsAppMessage(from, sb.toString());
		} catch (Exception e) {
			twilioService.sendWhatsAppMessage(from, "❌ Error retrieving category summary. Please try again.");
		}
	}

	public void handleDefaultTransaction(String from, Users user, String body) {
		try {
			WhatsAppMessageRequest reply = transactionHandler.handleTransactionMessage(user, body);
			twilioService.sendWhatsAppMessage(from, reply);
		} catch (Exception e) {
			twilioService.sendWhatsAppMessage(from,
			                                  """
				                                  🤔 I didn't quite get that.
				                                  
				                                  Type *help* to see available commands or\
				                                   try:
				                                  • spent 50 groceries
				                                  • received 1000 salary
				                                  • balance""");
		}
	}
}
