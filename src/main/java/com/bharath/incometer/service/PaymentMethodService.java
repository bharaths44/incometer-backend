package com.bharath.incometer.service;

import com.bharath.incometer.entities.DTOs.PaymentMethodRequestDTO;
import com.bharath.incometer.entities.DTOs.PaymentMethodResponseDTO;
import com.bharath.incometer.entities.PaymentMethod;
import com.bharath.incometer.entities.Users;
import com.bharath.incometer.enums.PaymentType;
import com.bharath.incometer.repository.PaymentMethodRepository;
import com.bharath.incometer.repository.UsersRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentMethodService {

	private final PaymentMethodRepository repository;
	private final UsersRepository usersRepository;


	public PaymentMethod savePaymentMethod(PaymentMethod paymentMethod, Long userId) {
		if (userId != null) {
			Users user = usersRepository.findById(userId).orElse(null);
			paymentMethod.setUser(user);
		}
		return repository.save(paymentMethod);
	}


	@Transactional()
	public List<PaymentMethodResponseDTO> getAll() {
		return repository.findAll().stream().map(this::toDTO).toList();
	}


	@Transactional()
	public Optional<PaymentMethodResponseDTO> getById(Long id) {
		return repository.findById(id).map(this::toDTO);
	}


	@Transactional()
	public List<PaymentMethodResponseDTO> getByUserId(Long userId) {
		return repository.findByUserUserId(userId).stream().map(this::toDTO).toList();
	}

	public PaymentMethod updatePaymentMethod(Long id, PaymentMethod update, Long userId) {
		PaymentMethod existing = repository.findById(id)
		                                   .orElseThrow(() -> new IllegalArgumentException(
			                                   "PaymentMethod not found: " + id));
		existing.setName(update.getName());
		existing.setDisplayName(update.getDisplayName());
		existing.setLastFourDigits(update.getLastFourDigits());
		existing.setIssuerName(update.getIssuerName());
		existing.setType(update.getType());
		existing.setIcon(update.getIcon());
		if (userId != null) {
			Users user = usersRepository.findById(userId).orElse(null);
			existing.setUser(user);
		}
		return repository.save(existing);
	}


	public void delete(Long id) {
		repository.deleteById(id);
	}


	public PaymentMethod findOrCreateByName(Long userId, String name) {
		if (name == null || name.trim().isEmpty()) {
			// Default to Cash or something
			name = "Cash";
		}
		String normalized = name.trim().toLowerCase();
		List<PaymentMethod> userMethods = repository.findByUserUserId(userId);
		for (PaymentMethod pm : userMethods) {
			if (pm.getName().toLowerCase().equals(normalized)) {
				return pm;
			}
		}
		// Create new one, assume type CASH for simplicity
		PaymentMethod newPm = new PaymentMethod();
		newPm.setName(name);
		newPm.setType(PaymentType.CASH);
		newPm.setDisplayName(name);
		newPm.setIcon("cash"); // or something
		return savePaymentMethod(newPm, userId);
	}

	public PaymentMethodResponseDTO toDTO(PaymentMethod pm) {
		return new PaymentMethodResponseDTO(
			pm.getPaymentMethodId(),
			pm.getName(),
			pm.getDisplayName(),
			pm.getLastFourDigits(),
			pm.getIssuerName(),
			pm.getType() != null ? pm.getType().name() : null,
			pm.getIcon(),
			pm.getCreatedAt()
		);
	}


	public PaymentMethod toEntity(PaymentMethodRequestDTO dto) {
		PaymentMethod pm = new PaymentMethod();
		pm.setName(dto.name());
		pm.setDisplayName(dto.displayName());
		pm.setLastFourDigits(dto.lastFourDigits());
		pm.setIssuerName(dto.issuerName());
		if (dto.type() != null) {
			try {
				pm.setType(PaymentType.valueOf(dto.type()));
			} catch (IllegalArgumentException e) {
				// Handle invalid type, perhaps set to OTHER or throw
				pm.setType(PaymentType.OTHER);
			}
		}
		pm.setIcon(dto.icon());
		return pm;
	}

	public PaymentMethodResponseDTO create(PaymentMethodRequestDTO dto, Long userId) {
		PaymentMethod entity = toEntity(dto);
		PaymentMethod created = savePaymentMethod(entity, userId);
		return toDTO(created);
	}

	public PaymentMethodResponseDTO update(Long id, PaymentMethodRequestDTO dto, Long userId) {
		PaymentMethod entity = toEntity(dto);
		PaymentMethod updated = updatePaymentMethod(id, entity, userId);
		return toDTO(updated);
	}
}
