package com.mycompany.myapp.service.mapper;

import com.mycompany.myapp.domain.Competitor;
import com.mycompany.myapp.domain.DataSource;
import com.mycompany.myapp.service.dto.CompetitorDTO;
import com.mycompany.myapp.service.dto.DataSourceDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link DataSource} and its DTO {@link DataSourceDTO}.
 */
@Mapper(componentModel = "spring")
public interface DataSourceMapper extends EntityMapper<DataSourceDTO, DataSource> {
    @Mapping(target = "competitor", source = "competitor", qualifiedByName = "competitorCompetitorName")
    DataSourceDTO toDto(DataSource s);

    @Mapping(target = "competitor", ignore = true)
    DataSource toEntity(DataSourceDTO dataSourceDTO);

    @Override
    @Named("partialUpdate")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "competitor", ignore = true)
    void partialUpdate(@MappingTarget DataSource entity, DataSourceDTO dto);

    @Named("competitorCompetitorName")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "competitorName", source = "competitorName")
    CompetitorDTO toDtoCompetitorCompetitorName(Competitor competitor);
}
