package com.example.ms_subscriptions.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionResponseDTO {
    private Long id;
    private String username;
    private String type;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean active;
}