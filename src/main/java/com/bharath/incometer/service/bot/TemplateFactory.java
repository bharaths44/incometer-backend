package com.bharath.incometer.service.bot;

import com.bharath.incometer.enums.TransactionType;
import com.bharath.incometer.models.PendingCategory;
import com.bharath.incometer.models.WhatsAppMessageRequest;
import com.bharath.incometer.utils.PaymentMethodFormatter;
import com.bharath.incometer.utils.TransactionExtractionResult;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.Map;

@Component
public class TemplateFactory {

    private final PaymentMethodFormatter paymentMethodFormatter;

    public TemplateFactory(PaymentMethodFormatter paymentMethodFormatter) {
        this.paymentMethodFormatter = paymentMethodFormatter;
    }

    public WhatsAppMessageRequest createConfirmationTemplate(TransactionExtractionResult result, TransactionType type) {
        Map<String, String> variables = Map.of("1",
                                               type == TransactionType.EXPENSE ? "Expense" : "Income",
                                               "2",
                                               result.amount().toString(),
                                               "3",
                                               result.categoryName(),
                                               "4",
                                               paymentMethodFormatter.toTitleCase(result.paymentMethod()),
                                               "5",
                                               result.date().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
        return new WhatsAppMessageRequest("HXc6b30b1dae1cfb277e3902dd8d9206b7", variables);
    }

    public WhatsAppMessageRequest createUndoTemplate(PendingCategory pending) {
        Map<String, String> variables = Map.of("1",
                                               (pending.type == TransactionType.EXPENSE ? "Expense" : "Income"),
                                               "2",
                                               pending.amount.toString(),
                                               "3",
                                               pending.suggestedCategory,
                                               "4",
                                               pending.paymentMethodName,
                                               "5",
                                               pending.transactionDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
        return new WhatsAppMessageRequest("HXef6fbb8080e20e794e21496f60d9c07d", variables);
    }
}
