package com.paseape.apipaseape.infrastructure.repository;

import com.paseape.apipaseape.application.repository.IBrevoEmailRepository;
import com.paseape.apipaseape.domain.entity.BrevoEmail;
import com.paseape.apipaseape.infrastructure.entity.BrevoEmailEntity;
import com.paseape.apipaseape.infrastructure.mapper.entity.IBrevoEmailDboMapper;
import com.paseape.apipaseape.infrastructure.repository.jpa.IBrevoEmailJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class BrevoEmailRepository implements IBrevoEmailRepository {

    private final IBrevoEmailJpaRepository jpaRepository;
    private final IBrevoEmailDboMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public BrevoEmail findById(Long id) {
        if (id == null) {
            return null;
        }
        BrevoEmailEntity entity = jpaRepository.findById(id.longValue());
        return entity != null ? mapper.toDomain(entity) : null;
    }

    @Override
    @Transactional(readOnly = true)
    public BrevoEmail findByUuid(String uuid) {
        if (uuid == null || uuid.trim().isEmpty()) {
            return null;
        }
        BrevoEmailEntity entity = jpaRepository.findByUuid(uuid.trim());
        return entity != null ? mapper.toDomain(entity) : null;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BrevoEmail> findAllByDestinatarioEmail(String destinatarioEmail) {
        if (destinatarioEmail == null || destinatarioEmail.trim().isEmpty()) {
            return List.of();
        }
        List<BrevoEmailEntity> entities = jpaRepository.findByDestinatarioEmail(destinatarioEmail.trim());
        return mapper.toDomainList(entities);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BrevoEmail> findAllByTipoNotificacion(String tipoNotificacion) {
        if (tipoNotificacion == null || tipoNotificacion.trim().isEmpty()) {
            return List.of();
        }
        List<BrevoEmailEntity> entities = jpaRepository.findByTipoNotificacion(tipoNotificacion.trim());
        return mapper.toDomainList(entities);
    }

    @Override
    @Transactional
    public BrevoEmail save(BrevoEmail brevoEmail) {
        BrevoEmailEntity entity = mapper.toEntity(brevoEmail);
        BrevoEmailEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }
}
