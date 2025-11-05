package com.bharath.incometer.entities.DTOs;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponseDTO(UUID userId,
                              String name,
                              String email,
                              String phoneNumber,
                              LocalDateTime createdAt,
                              LocalDateTime updatedAt) {
}
