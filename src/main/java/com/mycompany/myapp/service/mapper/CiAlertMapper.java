package com.mycompany.myapp.service.mapper;

import com.mycompany.myapp.domain.AnalysisResult;
import com.mycompany.myapp.domain.AnalystProfile;
import com.mycompany.myapp.domain.CiAlert;
import com.mycompany.myapp.service.dto.AnalysisResultDTO;
import com.mycompany.myapp.service.dto.AnalystProfileDTO;
import com.mycompany.myapp.service.dto.CiAlertDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link CiAlert} and its DTO {@link CiAlertDTO}.
 */
@Mapper(componentModel = "spring")
public interface CiAlertMapper extends EntityMapper<CiAlertDTO, CiAlert> {
    @Mapping(target = "analysisResult", source = "analysisResult", qualifiedByName = "analysisResultId")
    @Mapping(target = "analystProfile", source = "analystProfile", qualifiedByName = "analystProfileDisplayName")
    CiAlertDTO toDto(CiAlert s);

    @Named("analysisResultId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    AnalysisResultDTO toDtoAnalysisResultId(AnalysisResult analysisResult);

    @Named("analystProfileDisplayName")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "displayName", source = "displayName")
    AnalystProfileDTO toDtoAnalystProfileDisplayName(AnalystProfile analystProfile);
}
