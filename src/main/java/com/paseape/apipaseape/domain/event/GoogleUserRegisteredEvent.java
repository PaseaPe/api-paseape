package com.paseape.apipaseape.domain.event;

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
public class GoogleUserRegisteredEvent {

    private String usuarioUuid;
    private String googlePictureUrl;
}