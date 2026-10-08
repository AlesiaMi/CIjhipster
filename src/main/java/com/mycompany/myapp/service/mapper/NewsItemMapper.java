package com.mycompany.myapp.service.mapper;

import com.mycompany.myapp.domain.CollectionRun;
import com.mycompany.myapp.domain.Competitor;
import com.mycompany.myapp.domain.DataSource;
import com.mycompany.myapp.domain.NewsItem;
import com.mycompany.myapp.service.dto.CollectionRunDTO;
import com.mycompany.myapp.service.dto.CompetitorDTO;
import com.mycompany.myapp.service.dto.DataSourceDTO;
import com.mycompany.myapp.service.dto.NewsItemDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link NewsItem} and its DTO {@link NewsItemDTO}.
 */
@Mapper(componentModel = "spring")
public interface NewsItemMapper extends EntityMapper<NewsItemDTO, NewsItem> {
    @Mapping(target = "dataSource", source = "dataSource", qualifiedByName = "dataSourceSourceName")
    @Mapping(target = "competitor", source = "competitor", qualifiedByName = "competitorCompetitorName")
    @Mapping(target = "collectionRun", source = "collectionRun", qualifiedByName = "collectionRunId")
    NewsItemDTO toDto(NewsItem s);

    @Named("dataSourceSourceName")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "sourceName", source = "sourceName")
    DataSourceDTO toDtoDataSourceSourceName(DataSource dataSource);

    @Named("competitorCompetitorName")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "competitorName", source = "competitorName")
    CompetitorDTO toDtoCompetitorCompetitorName(Competitor competitor);

    @Named("collectionRunId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    CollectionRunDTO toDtoCollectionRunId(CollectionRun collectionRun);
}
