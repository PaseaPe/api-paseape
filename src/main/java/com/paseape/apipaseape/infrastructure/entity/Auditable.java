package com.paseape.apipaseape.infrastructure.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public class Auditable {
    @CreatedDate
    @Column(name = "creado_el", nullable = false, updatable = false)
    protected LocalDateTime creadoEl;

    @LastModifiedDate
    @Column(name = "actualizado_el")
    protected LocalDateTime actualizadoEl;

    @CreatedBy
    @Column(name = "creado_por", nullable = false, length = 100, updatable = false)
    protected String creadoPor;

    @LastModifiedBy
    @Column(name = "actualizado_por", length = 100)
    protected String actualizadoPor;
}
