package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.CollectionRun;
import com.mycompany.myapp.domain.Competitor;
import com.mycompany.myapp.domain.DataSource;
import com.mycompany.myapp.domain.NewsItem;
import com.mycompany.myapp.domain.enumeration.ManagerPermissionType;
import com.mycompany.myapp.repository.CollectionRunRepository;
import com.mycompany.myapp.repository.CompetitorRepository;
import com.mycompany.myapp.repository.DataSourceRepository;
import com.mycompany.myapp.repository.NewsItemRepository;
import com.mycompany.myapp.security.AuthoritiesConstants;
import com.mycompany.myapp.security.SecurityUtils;
import com.mycompany.myapp.service.dto.NewsItemDTO;
import com.mycompany.myapp.service.mapper.NewsItemMapper;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class NewsItemService {

    private static final Logger LOG = LoggerFactory.getLogger(NewsItemService.class);

    private final NewsItemRepository newsItemRepository;
    private final NewsItemMapper newsItemMapper;
    private final DataSourceRepository dataSourceRepository;
    private final CompetitorRepository competitorRepository;
    private final CollectionRunRepository collectionRunRepository;
    private final TenantCacheVersionService tenantCacheVersionService;
    private final ManagerAccessService managerAccessService;

    public NewsItemService(
        NewsItemRepository newsItemRepository,
        NewsItemMapper newsItemMapper,
        DataSourceRepository dataSourceRepository,
        CompetitorRepository competitorRepository,
        CollectionRunRepository collectionRunRepository,
        TenantCacheVersionService tenantCacheVersionService,
        ManagerAccessService managerAccessService
    ) {
        this.newsItemRepository = newsItemRepository;
        this.newsItemMapper = newsItemMapper;
        this.dataSourceRepository = dataSourceRepository;
        this.competitorRepository = competitorRepository;
        this.collectionRunRepository = collectionRunRepository;
        this.tenantCacheVersionService = tenantCacheVersionService;
        this.managerAccessService = managerAccessService;
    }

    public NewsItemDTO save(NewsItemDTO newsItemDTO) {
        LOG.debug("Request to save NewsItem : {}", newsItemDTO);

        ResolvedRelations relations = resolveRelations(newsItemDTO);

        NewsItem newsItem = newsItemMapper.toEntity(newsItemDTO);

        newsItem.setDataSource(relations.dataSource());
        newsItem.setCompetitor(relations.competitor());
        newsItem.setCollectionRun(relations.collectionRun());
        newsItem = newsItemRepository.save(newsItem);
        tenantCacheVersionService.invalidateAfterCommit(relations.ownerId());
        return newsItemMapper.toDto(newsItem);
    }

    public List<NewsItemDTO> saveImported(List<NewsItemDTO> newsItemDTOs) {
        LOG.debug("Request to save imported NewsItems : {} records", newsItemDTOs.size());

        List<NewsItem> newsItems = new ArrayList<>();
        Set<Long> ownerIds = new LinkedHashSet<>();

        for (NewsItemDTO newsItemDTO : newsItemDTOs) {
            if (newsItemDTO.getId() != null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Imported NewsItem cannot already have an id");
            }

            ResolvedRelations relations = resolveRelations(newsItemDTO);
            NewsItem newsItem = newsItemMapper.toEntity(newsItemDTO);

            newsItem.setDataSource(relations.dataSource());
            newsItem.setCompetitor(relations.competitor());
            newsItem.setCollectionRun(null);
            newsItems.add(newsItem);
            ownerIds.add(relations.ownerId());
        }
        List<NewsItem> savedNewsItems = newsItemRepository.saveAll(newsItems);
        ownerIds.forEach(tenantCacheVersionService::invalidateAfterCommit);
        return savedNewsItems.stream().map(newsItemMapper::toDto).toList();
    }

    public NewsItemDTO update(NewsItemDTO newsItemDTO) {
        LOG.debug("Request to update NewsItem : {}", newsItemDTO);

        Long id = newsItemDTO.getId();

        if (id == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "NewsItem id is required");
        }

        NewsItem existingNewsItem = findAccessibleEntity(id);

        Long oldOwnerId = existingNewsItem.getCompetitor().getOwner().getId();

        ResolvedRelations relations = resolveRelations(newsItemDTO);

        existingNewsItem.setExternalId(newsItemDTO.getExternalId());
        existingNewsItem.setTitle(newsItemDTO.getTitle());
        existingNewsItem.setUrl(newsItemDTO.getUrl());
        existingNewsItem.setOriginalText(newsItemDTO.getOriginalText());
        existingNewsItem.setPublishedAt(newsItemDTO.getPublishedAt());
        existingNewsItem.setCollectedAt(newsItemDTO.getCollectedAt());
        existingNewsItem.setIsDuplicate(newsItemDTO.getIsDuplicate());

        existingNewsItem.setDataSource(relations.dataSource());
        existingNewsItem.setCompetitor(relations.competitor());
        existingNewsItem.setCollectionRun(relations.collectionRun());

        existingNewsItem = newsItemRepository.save(existingNewsItem);

        invalidateOwners(oldOwnerId, relations.ownerId());

        return newsItemMapper.toDto(existingNewsItem);
    }

    public Optional<NewsItemDTO> partialUpdate(NewsItemDTO newsItemDTO) {
        LOG.debug("Request to partially update NewsItem : {}", newsItemDTO);
        Long id = newsItemDTO.getId();
        if (id == null) {
            return Optional.empty();
        }
        NewsItem existingNewsItem = findAccessibleEntity(id);

        Long oldOwnerId = existingNewsItem.getCompetitor().getOwner().getId();

        if (newsItemDTO.getExternalId() != null) {
            existingNewsItem.setExternalId(newsItemDTO.getExternalId());
        }

        if (newsItemDTO.getTitle() != null) {
            existingNewsItem.setTitle(newsItemDTO.getTitle());
        }

        if (newsItemDTO.getUrl() != null) {
            existingNewsItem.setUrl(newsItemDTO.getUrl());
        }

        if (newsItemDTO.getOriginalText() != null) {
            existingNewsItem.setOriginalText(newsItemDTO.getOriginalText());
        }

        if (newsItemDTO.getPublishedAt() != null) {
            existingNewsItem.setPublishedAt(newsItemDTO.getPublishedAt());
        }

        if (newsItemDTO.getCollectedAt() != null) {
            existingNewsItem.setCollectedAt(newsItemDTO.getCollectedAt());
        }

        if (newsItemDTO.getIsDuplicate() != null) {
            existingNewsItem.setIsDuplicate(newsItemDTO.getIsDuplicate());
        }

        Long newOwnerId = oldOwnerId;

        boolean relationsChanged =
            newsItemDTO.getDataSource() != null || newsItemDTO.getCompetitor() != null || newsItemDTO.getCollectionRun() != null;

        if (relationsChanged) {
            NewsItemDTO relationDTO = buildRelationDTOForPartialUpdate(newsItemDTO, existingNewsItem);

            ResolvedRelations relations = resolveRelations(relationDTO);

            existingNewsItem.setDataSource(relations.dataSource());
            existingNewsItem.setCompetitor(relations.competitor());
            existingNewsItem.setCollectionRun(relations.collectionRun());

            newOwnerId = relations.ownerId();
        }

        existingNewsItem = newsItemRepository.save(existingNewsItem);

        invalidateOwners(oldOwnerId, newOwnerId);

        return Optional.of(newsItemMapper.toDto(existingNewsItem));
    }

    public Page<NewsItemDTO> findAllWithEagerRelationships(Pageable pageable) {
        return newsItemRepository.findAllWithEagerRelationships(pageable).map(newsItemMapper::toDto);
    }

    @Transactional(readOnly = true)
    public List<NewsItemDTO> findAllWhereAnalysisResultIsNull() {
        LOG.debug("Request to get all newsItems where AnalysisResult is null");

        return StreamSupport.stream(newsItemRepository.findAll().spliterator(), false)
            .filter(newsItem -> newsItem.getAnalysisResult() == null)
            .map(newsItemMapper::toDto)
            .collect(Collectors.toCollection(LinkedList::new));
    }

    @Transactional(readOnly = true)
    public Optional<NewsItemDTO> findOne(Long id) {
        LOG.debug("Request to get NewsItem : {}", id);

        return newsItemRepository
            .findOneWithEagerRelationships(id)
            .filter(newsItem -> {
                Long ownerId = newsItem.getCompetitor().getOwner().getId();

                return managerAccessService.hasPermission(ownerId, ManagerPermissionType.NEWS_VIEW);
            })
            .map(newsItemMapper::toDto);
    }

    public void delete(Long id) {
        LOG.debug("Request to delete NewsItem : {}", id);

        NewsItem newsItem = findAccessibleEntity(id);

        Long ownerId = newsItem.getCompetitor().getOwner().getId();

        newsItemRepository.delete(newsItem);

        tenantCacheVersionService.invalidateAfterCommit(ownerId);
    }

    @Transactional(readOnly = true)
    public boolean canAccess(Long id) {
        if (isAdmin()) {
            return newsItemRepository.existsById(id);
        }

        Long currentUserId = requireCurrentUserId();

        return newsItemRepository.existsByIdAndCompetitorOwnerId(id, currentUserId);
    }

    private NewsItem findAccessibleEntity(Long id) {
        if (isAdmin()) {
            return newsItemRepository
                .findOneWithEagerRelationships(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "NewsItem not found"));
        }

        Long currentUserId = requireCurrentUserId();

        return newsItemRepository
            .findOneByIdAndCompetitorOwnerId(id, currentUserId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "NewsItem not found"));
    }

    private ResolvedRelations resolveRelations(NewsItemDTO dto) {
        if (dto.getCompetitor() == null || dto.getCompetitor().getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Competitor is required");
        }

        if (dto.getDataSource() == null || dto.getDataSource().getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "DataSource is required");
        }

        Long competitorId = dto.getCompetitor().getId();

        Long dataSourceId = dto.getDataSource().getId();

        Competitor competitor;
        DataSource dataSource;

        if (isAdmin()) {
            competitor = competitorRepository
                .findById(competitorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Competitor not found"));

            Long ownerId = competitor.getOwner().getId();

            dataSource = dataSourceRepository
                .findOneByIdAndCompetitorOwnerId(dataSourceId, ownerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "DataSource belongs to another owner"));
        } else {
            Long currentUserId = requireCurrentUserId();

            competitor = competitorRepository
                .findOneByIdAndOwnerId(competitorId, currentUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Competitor not found"));

            dataSource = dataSourceRepository
                .findOneByIdAndCompetitorOwnerId(dataSourceId, currentUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "DataSource not found"));
        }

        if (dataSource.getCompetitor() == null || !dataSource.getCompetitor().getId().equals(competitor.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "DataSource does not belong to selected Competitor");
        }

        Long ownerId = competitor.getOwner().getId();

        CollectionRun collectionRun = null;

        if (dto.getCollectionRun() != null && dto.getCollectionRun().getId() != null) {
            Long collectionRunId = dto.getCollectionRun().getId();

            collectionRun = collectionRunRepository
                .findOneByIdAndOwnerId(collectionRunId, ownerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "CollectionRun belongs to another owner"));
        }

        return new ResolvedRelations(dataSource, competitor, collectionRun, ownerId);
    }

    private NewsItemDTO buildRelationDTOForPartialUpdate(NewsItemDTO patch, NewsItem existing) {
        NewsItemDTO dto = new NewsItemDTO();

        dto.setCompetitor(patch.getCompetitor() != null ? patch.getCompetitor() : newsItemMapper.toDto(existing).getCompetitor());

        dto.setDataSource(patch.getDataSource() != null ? patch.getDataSource() : newsItemMapper.toDto(existing).getDataSource());

        dto.setCollectionRun(
            patch.getCollectionRun() != null ? patch.getCollectionRun() : newsItemMapper.toDto(existing).getCollectionRun()
        );

        return dto;
    }

    private void invalidateOwners(Long oldOwnerId, Long newOwnerId) {
        tenantCacheVersionService.invalidateAfterCommit(oldOwnerId);

        if (newOwnerId != null && !newOwnerId.equals(oldOwnerId)) {
            tenantCacheVersionService.invalidateAfterCommit(newOwnerId);
        }
    }

    private Long requireCurrentUserId() {
        return SecurityUtils.getCurrentUserId().orElseThrow(() ->
            new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Current user id not found")
        );
    }

    private boolean isAdmin() {
        return SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.ADMIN);
    }

    private record ResolvedRelations(DataSource dataSource, Competitor competitor, CollectionRun collectionRun, Long ownerId) {}
}
