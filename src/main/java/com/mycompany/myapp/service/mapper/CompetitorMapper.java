package com.mycompany.myapp.service.mapper;

import com.mycompany.myapp.domain.AnalystProfile;
import com.mycompany.myapp.domain.Competitor;
import com.mycompany.myapp.service.dto.AnalystProfileDTO;
import com.mycompany.myapp.service.dto.CompetitorDTO;
import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Competitor} and its DTO {@link CompetitorDTO}.
 */
@Mapper(componentModel = "spring")
public interface CompetitorMapper extends EntityMapper<CompetitorDTO, Competitor> {
    @Mapping(target = "analystProfileses", source = "analystProfileses", qualifiedByName = "analystProfileDisplayNameSet")
    CompetitorDTO toDto(Competitor s);

    @Mapping(target = "analystProfileses", ignore = true)
    @Mapping(target = "removeAnalystProfiles", ignore = true)
    Competitor toEntity(CompetitorDTO competitorDTO);

    @Named("analystProfileDisplayName")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "displayName", source = "displayName")
    AnalystProfileDTO toDtoAnalystProfileDisplayName(AnalystProfile analystProfile);

    @Named("analystProfileDisplayNameSet")
    default Set<AnalystProfileDTO> toDtoAnalystProfileDisplayNameSet(Set<AnalystProfile> analystProfile) {
        return analystProfile.stream().map(this::toDtoAnalystProfileDisplayName).collect(Collectors.toSet());
    }
}
