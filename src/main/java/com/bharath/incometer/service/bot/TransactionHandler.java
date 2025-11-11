package com.bharath.incometer.service.bot;

import com.bharath.incometer.entities.Users;
import com.bharath.incometer.enums.TransactionType;
import com.bharath.incometer.models.PendingCategory;
import com.bharath.incometer.service.CategoryService;
import com.bharath.incometer.service.PaymentMethodService;
import com.bharath.incometer.models.WhatsAppMessageRequest;
import com.bharath.incometer.utils.CategoryMatchingFuzzy;
import com.bharath.incometer.utils.PaymentMethodFormatter;
import com.bharath.incometer.utils.TransactionExtractionResult;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Component
public class TransactionHandler {

    private final GeminiExtractionService geminiExtractionService;
    private final CategoryMatchingFuzzy categoryMatchingFuzzy;
    private final PaymentMethodFormatter paymentMethodFormatter;
    private final PaymentMethodService paymentMethodService;
    private final CategoryService categoryService;
    private final TemplateFactory templateFactory;

    public TransactionHandler(GeminiExtractionService geminiExtractionService,
                              CategoryMatchingFuzzy categoryMatchingFuzzy,
                              PaymentMethodFormatter paymentMethodFormatter,
                              PaymentMethodService paymentMethodService,
                              CategoryService categoryService,
                              TemplateFactory templateFactory) {
        this.geminiExtractionService = geminiExtractionService;
        this.categoryMatchingFuzzy = categoryMatchingFuzzy;
        this.paymentMethodFormatter = paymentMethodFormatter;
        this.paymentMethodService = paymentMethodService;
        this.categoryService = categoryService;
        this.templateFactory = templateFactory;
    }

    public WhatsAppMessageRequest handleTransaction(Users user, String body, Map<String, PendingCategory> pendingMap,
                                                    TransactionType type) {
        TransactionExtractionResult result = geminiExtractionService.extractTransaction(body, type);

        if (result.amount() == null) {
            return new WhatsAppMessageRequest("❌ Could not detect amount. Include a numeric value.");
        }

        Long categoryId = categoryService.getCategoryIdByName(result.categoryName(), user.getUserId());
        if (categoryId != null) {
            PendingCategory pending = createPending(user, result, type, categoryId, "confirmation");
            pendingMap.put(user.getPhoneNumber(), pending);
            return templateFactory.createConfirmationTemplate(result, type);
        }

        List<String> existingCategories = categoryService.getAllCategoryNamesForUserByType(user.getUserId(), type);
        String closest = categoryMatchingFuzzy.findClosestCategory(result.categoryName(), existingCategories);

        if (closest != null) {
            PendingCategory pending = createPending(user, result, type, null, "suggestion");
            pending.suggestedCategory = closest;
            pendingMap.put(user.getPhoneNumber(), pending);
            return new WhatsAppMessageRequest(
                "I don't recognise the category " + result.categoryName() + ". Did you mean: 1) " + closest +
                " 2) Create new? Reply with the number.");
        }

        PendingCategory pending = createPending(user, result, type, null, "creation");
        pendingMap.put(user.getPhoneNumber(), pending);
        return new WhatsAppMessageRequest(
            "❓ Category '" + result.categoryName() + "' not found. Do you want to create it? Reply 'Yes' or 'No'.");
    }

    private PendingCategory createPending(Users user, TransactionExtractionResult result, TransactionType type,
                                          Long categoryId, String mode) {
        PendingCategory pending = new PendingCategory();
        pending.userId = user.getUserId();
        pending.amount = result.amount();
        pending.suggestedCategory = result.categoryName();
        pending.originalCategoryName = result.categoryName();
        pending.paymentMethodId = paymentMethodService.findOrCreateByName(user.getUserId(), result.paymentMethod())
                                                      .getPaymentMethodId();
        pending.transactionDate = result.date();
        pending.type = type;
        pending.categoryId = categoryId;
        pending.mode = mode;
        pending.paymentMethodName = paymentMethodFormatter.toTitleCase(result.paymentMethod());
        pending.createdAt = LocalDateTime.now();
        return pending;
    }
}
