package com.bharath.incometer.service;

import com.bharath.incometer.entities.Category;
import com.bharath.incometer.entities.DTOs.TransactionRequestDTO;
import com.bharath.incometer.entities.DTOs.TransactionResponseDTO;
import com.bharath.incometer.entities.PaymentMethod;
import com.bharath.incometer.entities.Transaction;
import com.bharath.incometer.entities.Users;
import com.bharath.incometer.enums.TransactionType;
import com.bharath.incometer.repository.CategoryRepository;
import com.bharath.incometer.repository.PaymentMethodRepository;
import com.bharath.incometer.repository.TransactionRepository;
import com.bharath.incometer.repository.UsersRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionService {

	private final TransactionRepository transactionRepository;
	private final CategoryRepository categoryRepository;
	private final UsersRepository usersRepository;
	private final PaymentMethodRepository paymentMethodRepository;

	public TransactionService(TransactionRepository transactionRepository, CategoryRepository categoryRepository,
	                          UsersRepository usersRepository, PaymentMethodRepository paymentMethodRepository) {
		this.transactionRepository = transactionRepository;
		this.categoryRepository = categoryRepository;
		this.usersRepository = usersRepository;
		this.paymentMethodRepository = paymentMethodRepository;
	}

	private TransactionResponseDTO toDTO(Transaction transaction) {
		return new TransactionResponseDTO(transaction.getTransactionId(),
		                                  transaction.getUser().getUserId(),
		                                  new TransactionResponseDTO.CategoryDto(transaction.getCategory()
		                                                                                    .getCategoryId(),
		                                                                         transaction.getCategory().getName(),
		                                                                         transaction.getCategory().getIcon()),
		                                  transaction.getAmount(),
		                                  transaction.getDescription(),
		                                  new TransactionResponseDTO.PaymentMethodDto(transaction.getPaymentMethod()
		                                                                                         .getPaymentMethodId(),
		                                                                              transaction.getPaymentMethod()
		                                                                                         .getName(),
		                                                                              transaction.getPaymentMethod()
		                                                                                         .getDisplayName(),
		                                                                              transaction.getPaymentMethod()
		                                                                                         .getType()
		                                                                                         .name()),
		                                  transaction.getTransactionDate(),
		                                  transaction.getTransactionType(),
		                                  transaction.getCreatedAt());
	}

	private Transaction toEntity(TransactionRequestDTO transactionRequestDTO) {
		Transaction transaction = new Transaction();

		Users user = usersRepository.findById(transactionRequestDTO.userId())
		                            .orElseThrow(() -> new RuntimeException(
			                            "User not found with id: " + transactionRequestDTO.userId()));

		Category category = categoryRepository.findById(transactionRequestDTO.categoryId())
		                                      .orElseThrow(() -> new RuntimeException(
			                                      "Category not found with id: " + transactionRequestDTO.categoryId()));

		// Validate that category belongs to the user
		if (!category.getUser().getUserId().equals(transactionRequestDTO.userId())) {
			throw new RuntimeException("Category does not belong to the specified user");
		}

		PaymentMethod paymentMethod = paymentMethodRepository.findById(transactionRequestDTO.paymentMethodId())
		                                                     .orElseThrow(() -> new RuntimeException(
			                                                     "PaymentMethod not found with id: " +
			                                                     transactionRequestDTO.paymentMethodId()));

		transaction.setUser(user);
		transaction.setCategory(category);
		transaction.setAmount(transactionRequestDTO.amount());
		transaction.setDescription(transactionRequestDTO.description());
		transaction.setPaymentMethod(paymentMethod);
		transaction.setTransactionDate(transactionRequestDTO.transactionDate());
		transaction.setTransactionType(transactionRequestDTO.transactionType());

		return transaction;
	}

	private void validateTransactionRequest(TransactionRequestDTO dto) {
		if (dto == null) {
			throw new IllegalArgumentException("Transaction request cannot be null");
		}

		if (dto.userId() == null) {
			throw new IllegalArgumentException("User ID cannot be null");
		}

		if (dto.categoryId() == null) {
			throw new IllegalArgumentException("Category ID cannot be null");
		}

		if (dto.amount() == null || dto.amount().compareTo(BigDecimal.ZERO) <= 0) {
			throw new IllegalArgumentException("Amount must be greater than zero");
		}

		if (dto.transactionDate() == null) {
			throw new IllegalArgumentException("Transaction date cannot be null");
		}

		if (dto.transactionDate().isAfter(LocalDate.now())) {
			throw new IllegalArgumentException("Transaction date cannot be in the future");
		}

		if (dto.paymentMethodId() == null) {
			throw new IllegalArgumentException("Payment method ID cannot be null");
		}

		if (dto.transactionType() == null) {
			throw new IllegalArgumentException("Transaction type cannot be null");
		}
	}

	@Transactional
	public TransactionResponseDTO createTransaction(TransactionRequestDTO dto) {
		validateTransactionRequest(dto);

		Transaction transaction = toEntity(dto);
		Transaction saved = transactionRepository.save(transaction);
		return toDTO(saved);
	}

	@Transactional
	public TransactionResponseDTO updateTransaction(Long transactionId, TransactionRequestDTO dto) {
		if (transactionId == null) {
			throw new IllegalArgumentException("Transaction ID cannot be null");
		}

		validateTransactionRequest(dto);

		Transaction transaction = transactionRepository.findById(transactionId)
		                                               .orElseThrow(() -> new RuntimeException(
			                                               "Transaction not found with id: " + transactionId));

		// Security check: ensure the transaction belongs to the user making the request
		if (!transaction.getUser().getUserId().equals(dto.userId())) {
			throw new RuntimeException("User does not have permission to update this transaction");
		}

		// Validate and update category
		Category category = categoryRepository.findById(dto.categoryId())
		                                      .orElseThrow(() -> new RuntimeException(
			                                      "Category not found with id: " + dto.categoryId()));

		if (!category.getUser().getUserId().equals(dto.userId())) {
			throw new RuntimeException("Category does not belong to the specified user");
		}

		PaymentMethod paymentMethod = paymentMethodRepository.findById(dto.paymentMethodId())
		                                                     .orElseThrow(() -> new RuntimeException(
			                                                     "PaymentMethod not found with id: " +
			                                                     dto.paymentMethodId()));

		transaction.setCategory(category);
		transaction.setAmount(dto.amount());
		transaction.setDescription(dto.description());
		transaction.setPaymentMethod(paymentMethod);
		transaction.setTransactionDate(dto.transactionDate());
		transaction.setTransactionType(dto.transactionType());

		Transaction updated = transactionRepository.save(transaction);
		return toDTO(updated);
	}

	@Transactional
	public void deleteTransaction(Long id, Long userId) {
		if (id == null) {
			throw new IllegalArgumentException("Transaction ID cannot be null");
		}

		if (userId == null) {
			throw new IllegalArgumentException("User ID cannot be null");
		}

		Transaction transaction = transactionRepository.findById(id)
		                                               .orElseThrow(() -> new RuntimeException(
			                                               "Transaction not found with id: " + id));

		// Security check: ensure the transaction belongs to the user
		if (!transaction.getUser().getUserId().equals(userId)) {
			throw new RuntimeException("User does not have permission to delete this transaction");
		}

		transactionRepository.delete(transaction);
	}

	@Transactional(readOnly = true)
	public TransactionResponseDTO getTransactionById(Long id, Long userId) {
		if (id == null) {
			throw new IllegalArgumentException("Transaction ID cannot be null");
		}

		Transaction transaction = transactionRepository.findById(id)
		                                               .orElseThrow(() -> new RuntimeException(
			                                               "Transaction not found with id: " + id));

		// Security check
		if (!transaction.getUser().getUserId().equals(userId)) {
			throw new RuntimeException("User does not have permission to view this transaction");
		}

		return toDTO(transaction);
	}

	@Transactional(readOnly = true)
	public List<TransactionResponseDTO> getTransactionsByUserId(Long userId) {
		if (userId == null) {
			throw new IllegalArgumentException("User ID cannot be null");
		}

		return transactionRepository.findByUserUserId(userId).stream().map(this::toDTO).collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public List<TransactionResponseDTO> getTransactionsByUserIdAndType(Long userId, TransactionType transactionType) {
		if (userId == null) {
			throw new IllegalArgumentException("User ID cannot be null");
		}

		if (transactionType == null) {
			throw new IllegalArgumentException("Transaction type cannot be null");
		}

		return transactionRepository.findByUserUserIdAndTransactionType(userId, transactionType)
		                            .stream()
		                            .map(this::toDTO)
		                            .collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public List<TransactionResponseDTO> getTransactionsByUserIdAndDateRange(Long userId, LocalDate startDate,
	                                                                        LocalDate endDate) {

		if (userId == null) {
			throw new IllegalArgumentException("User ID cannot be null");
		}

		if (startDate == null || endDate == null) {
			throw new IllegalArgumentException("Start date and end date cannot be null");
		}

		if (startDate.isAfter(endDate)) {
			throw new IllegalArgumentException("Start date cannot be after end date");
		}

		return transactionRepository.findByUserUserIdAndTransactionDateBetween(userId, startDate, endDate)
		                            .stream()
		                            .map(this::toDTO)
		                            .collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public List<TransactionResponseDTO> getTransactionsByUserIdAndTypeAndDateRange(Long userId,
	                                                                               TransactionType transactionType,
	                                                                               LocalDate startDate,
	                                                                               LocalDate endDate) {

		if (userId == null) {
			throw new IllegalArgumentException("User ID cannot be null");
		}

		if (transactionType == null) {
			throw new IllegalArgumentException("Transaction type cannot be null");
		}

		if (startDate == null || endDate == null) {
			throw new IllegalArgumentException("Start date and end date cannot be null");
		}

		if (startDate.isAfter(endDate)) {
			throw new IllegalArgumentException("Start date cannot be after end date");
		}

		return transactionRepository.findByUserUserIdAndTransactionTypeAndTransactionDateBetween(userId,
		                                                                                         transactionType,
		                                                                                         startDate,
		                                                                                         endDate)
		                            .stream()
		                            .map(this::toDTO)
		                            .collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public BigDecimal getTotalAmountByUserId(Long userId) {
		if (userId == null) {
			throw new IllegalArgumentException("User ID cannot be null");
		}

		return transactionRepository.sumAmountByUserId(userId);
	}

	@Transactional(readOnly = true)
	public BigDecimal getTotalAmountByUserIdAndType(Long userId, TransactionType transactionType) {
		if (userId == null) {
			throw new IllegalArgumentException("User ID cannot be null");
		}

		if (transactionType == null) {
			throw new IllegalArgumentException("Transaction type cannot be null");
		}

		return transactionRepository.sumAmountByUserIdAndType(userId, transactionType);
	}
}