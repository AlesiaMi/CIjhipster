package com.mycompany.myapp.service.mapper;

import com.mycompany.myapp.domain.Competitor;
import com.mycompany.myapp.domain.Keyword;
import com.mycompany.myapp.service.dto.CompetitorDTO;
import com.mycompany.myapp.service.dto.KeywordDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Keyword} and its DTO {@link KeywordDTO}.
 */
@Mapper(componentModel = "spring")
public interface KeywordMapper extends EntityMapper<KeywordDTO, Keyword> {
    @Mapping(target = "competitor", source = "competitor", qualifiedByName = "competitorCompetitorName")
    KeywordDTO toDto(Keyword s);

    @Named("competitorCompetitorName")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "competitorName", source = "competitorName")
    CompetitorDTO toDtoCompetitorCompetitorName(Competitor competitor);
}
