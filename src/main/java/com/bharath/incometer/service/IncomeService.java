package com.bharath.incometer.service;

import com.bharath.incometer.entities.DTOs.IncomeRequestDTO;
import com.bharath.incometer.entities.DTOs.IncomeResponseDTO;
import com.bharath.incometer.entities.Income;
import com.bharath.incometer.entities.Users;
import com.bharath.incometer.repository.IncomeRepository;
import com.bharath.incometer.repository.UsersRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class IncomeService {

	private final IncomeRepository incomeRepository;
	private final UsersRepository usersRepository;

	public IncomeService(IncomeRepository incomeRepository, UsersRepository usersRepository) {
		this.incomeRepository = incomeRepository;
		this.usersRepository = usersRepository;
	}

	private IncomeResponseDTO toDTO(Income income) {
		return new IncomeResponseDTO(
			income.getIncomeId(),
			income.getUser()
			      .getUserId(),
			income.getSource(),
			income.getAmount(),
			income.getReceivedDate(),
			income.getCreatedAt()
		);
	}

	private Income toEntity(IncomeRequestDTO incomeRequestDTO) {
		Income income = new Income();

		Users user = usersRepository.findById(incomeRequestDTO.userId())
		                            .orElseThrow(() -> new RuntimeException(
			                            "User not found with id: " + incomeRequestDTO.userId()));

		income.setUser(user);
		income.setSource(incomeRequestDTO.source());
		income.setAmount(incomeRequestDTO.amount());
		income.setReceivedDate(incomeRequestDTO.receivedDate());

		return income;
	}

	private void validateIncomeRequest(IncomeRequestDTO dto) {
		if (dto == null) {
			throw new IllegalArgumentException("Income request cannot be null");
		}

		if (dto.userId() == null) {
			throw new IllegalArgumentException("User ID cannot be null");
		}

		if (dto.amount() == null || dto.amount()
		                               .compareTo(BigDecimal.ZERO) <= 0) {
			throw new IllegalArgumentException("Amount must be greater than zero");
		}

		if (dto.receivedDate() != null && dto.receivedDate()
		                                     .isAfter(LocalDate.now())) {
			throw new IllegalArgumentException("Received date cannot be in the future");
		}
	}

	@Transactional
	public IncomeResponseDTO createIncome(IncomeRequestDTO dto) {
		validateIncomeRequest(dto);

		Income income = toEntity(dto);
		Income saved = incomeRepository.save(income);
		return toDTO(saved);
	}

	@Transactional
	public IncomeResponseDTO updateIncome(Long incomeId, IncomeRequestDTO dto) {
		if (incomeId == null) {
			throw new IllegalArgumentException("Income ID cannot be null");
		}

		validateIncomeRequest(dto);

		Income income = incomeRepository.findById(incomeId)
		                                .orElseThrow(() -> new RuntimeException(
			                                "Income not found with id: " + incomeId));

		// Security check: ensure the income belongs to the user making the request
		if (!income.getUser()
		           .getUserId()
		           .equals(dto.userId())) {
			throw new RuntimeException("User does not have permission to update this income");
		}

		income.setSource(dto.source());
		income.setAmount(dto.amount());
		income.setReceivedDate(dto.receivedDate());

		Income updated = incomeRepository.save(income);
		return toDTO(updated);
	}

	@Transactional
	public void deleteIncome(Long id, Long userId) {
		if (id == null) {
			throw new IllegalArgumentException("Income ID cannot be null");
		}

		if (userId == null) {
			throw new IllegalArgumentException("User ID cannot be null");
		}

		Income income = incomeRepository.findById(id)
		                                .orElseThrow(() -> new RuntimeException("Income not found with id: " + id));

		// Security check: ensure the income belongs to the user
		if (!income.getUser()
		           .getUserId()
		           .equals(userId)) {
			throw new RuntimeException("User does not have permission to delete this income");
		}

		incomeRepository.delete(income);
	}

	@Transactional(readOnly = true)
	public IncomeResponseDTO getIncomeById(Long id, Long userId) {
		if (id == null) {
			throw new IllegalArgumentException("Income ID cannot be null");
		}

		Income income = incomeRepository.findById(id)
		                                .orElseThrow(() -> new RuntimeException("Income not found with id: " + id));

		// Security check
		if (!income.getUser()
		           .getUserId()
		           .equals(userId)) {
			throw new RuntimeException("User does not have permission to view this income");
		}

		return toDTO(income);
	}

	@Transactional(readOnly = true)
	public List<IncomeResponseDTO> getAllIncomes() {
		return incomeRepository.findAll()
		                       .stream()
		                       .map(this::toDTO)
		                       .collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public List<IncomeResponseDTO> getIncomesByUserId(Long userId) {
		if (userId == null) {
			throw new IllegalArgumentException("User ID cannot be null");
		}

		return incomeRepository.findByUserUserId(userId)
		                       .stream()
		                       .map(this::toDTO)
		                       .collect(Collectors.toList());
	}
}
