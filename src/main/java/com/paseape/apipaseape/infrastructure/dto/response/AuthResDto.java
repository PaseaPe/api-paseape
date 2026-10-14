package com.paseape.apipaseape.infrastructure.dto.response;

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
public class AuthResDto {
    private Long id;
    private String uuid;
    private String email;
    private String fullName;
    private String pictureUrl;
    private String role;
    private Integer userTypeId;
    private String token;
}
