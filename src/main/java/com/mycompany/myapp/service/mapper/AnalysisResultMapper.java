package com.mycompany.myapp.service.mapper;

import com.mycompany.myapp.domain.AnalysisResult;
import com.mycompany.myapp.domain.NewsItem;
import com.mycompany.myapp.service.dto.AnalysisResultDTO;
import com.mycompany.myapp.service.dto.NewsItemDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link AnalysisResult} and its DTO {@link AnalysisResultDTO}.
 */
@Mapper(componentModel = "spring")
public interface AnalysisResultMapper extends EntityMapper<AnalysisResultDTO, AnalysisResult> {
    @Mapping(target = "newsItem", source = "newsItem", qualifiedByName = "newsItemTitle")
    AnalysisResultDTO toDto(AnalysisResult s);

    @Named("newsItemTitle")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "title", source = "title")
    NewsItemDTO toDtoNewsItemTitle(NewsItem newsItem);
}
