package com.mycompany.myapp.service.mapper;

import com.mycompany.myapp.domain.AnalystProfile;
import com.mycompany.myapp.domain.Competitor;
import com.mycompany.myapp.domain.User;
import com.mycompany.myapp.service.dto.AnalystProfileDTO;
import com.mycompany.myapp.service.dto.CompetitorDTO;
import com.mycompany.myapp.service.dto.UserDTO;
import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link AnalystProfile} and its DTO {@link AnalystProfileDTO}.
 */
@Mapper(componentModel = "spring")
public interface AnalystProfileMapper extends EntityMapper<AnalystProfileDTO, AnalystProfile> {
    @Mapping(target = "user", source = "user", qualifiedByName = "userLogin")
    @Mapping(target = "competitorses", source = "competitorses", qualifiedByName = "competitorCompetitorNameSet")
    AnalystProfileDTO toDto(AnalystProfile s);

    @Mapping(target = "removeCompetitors", ignore = true)
    AnalystProfile toEntity(AnalystProfileDTO analystProfileDTO);

    @Named("userLogin")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "login", source = "login")
    UserDTO toDtoUserLogin(User user);

    @Named("competitorCompetitorName")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "competitorName", source = "competitorName")
    CompetitorDTO toDtoCompetitorCompetitorName(Competitor competitor);

    @Named("competitorCompetitorNameSet")
    default Set<CompetitorDTO> toDtoCompetitorCompetitorNameSet(Set<Competitor> competitor) {
        return competitor.stream().map(this::toDtoCompetitorCompetitorName).collect(Collectors.toSet());
    }
}
