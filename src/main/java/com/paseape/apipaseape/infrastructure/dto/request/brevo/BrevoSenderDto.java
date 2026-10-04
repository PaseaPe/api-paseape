package com.paseape.apipaseape.infrastructure.dto.request.brevo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BrevoSenderDto {
    private String name;
    private String email;
}
