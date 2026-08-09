package com.mycompany.myapp.service.mapper;

import com.mycompany.myapp.domain.CollectionRun;
import com.mycompany.myapp.service.dto.CollectionRunDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link CollectionRun} and its DTO {@link CollectionRunDTO}.
 */
@Mapper(componentModel = "spring")
public interface CollectionRunMapper extends EntityMapper<CollectionRunDTO, CollectionRun> {}
