package com.paseape.apipaseape.infrastructure.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.paseape.apipaseape.application.repository.ITipoDocumentoRepository;
import com.paseape.apipaseape.domain.entity.TipoDocumento;
import com.paseape.apipaseape.infrastructure.entity.TipoDocumentoEntity;
import com.paseape.apipaseape.infrastructure.mapper.entity.ITipoDocumentoDboMapper;
import com.paseape.apipaseape.infrastructure.repository.jpa.ITipoDocumentoJpaRepository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class TipoDocumentoRepository implements ITipoDocumentoRepository {

    private final ITipoDocumentoJpaRepository jpaRepository;
    private final ITipoDocumentoDboMapper mapper;

    @Override
    public TipoDocumento findById(Integer id) {
        if (id == null) {
            return null;
        }
        TipoDocumentoEntity entity = jpaRepository.findById(id.intValue());
        return entity != null ? mapper.toDomain(entity) : null;
    }

    @Override
    public TipoDocumento findByUuid(String uuid) {
        if (uuid == null || uuid.trim().isEmpty()) {
            return null;
        }
        TipoDocumentoEntity entity = jpaRepository.findByUuid(uuid.trim());
        return entity != null ? mapper.toDomain(entity) : null;
    }

    @Override
    public TipoDocumento findByDescripcion(String descripcion) {
        if (descripcion == null || descripcion.trim().isEmpty()) {
            return null;
        }
        TipoDocumentoEntity entity = jpaRepository.findByDescripcion(descripcion.trim());
        return entity != null ? mapper.toDomain(entity) : null;
    }

    @Override
    public List<TipoDocumento> findAllByEstado(Integer estado) {
        List<TipoDocumentoEntity> entities = jpaRepository.findByEstado(estado);
        return mapper.toDomainList(entities);
    }

    @Override
    public TipoDocumento save(TipoDocumento tipoDocumento) {
        TipoDocumentoEntity entity = mapper.toEntity(tipoDocumento);
        TipoDocumentoEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }
}
