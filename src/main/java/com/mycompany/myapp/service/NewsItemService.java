package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.NewsItem;
import com.mycompany.myapp.repository.NewsItemRepository;
import com.mycompany.myapp.service.dto.NewsItemDTO;
import com.mycompany.myapp.service.mapper.NewsItemMapper;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class NewsItemService {

    private static final Logger LOG = LoggerFactory.getLogger(NewsItemService.class);

    private final NewsItemRepository newsItemRepository;

    private final NewsItemMapper newsItemMapper;

    public NewsItemService(NewsItemRepository newsItemRepository, NewsItemMapper newsItemMapper) {
        this.newsItemRepository = newsItemRepository;
        this.newsItemMapper = newsItemMapper;
    }

    @CacheEvict(cacheNames = "newsItemsPages", allEntries = true)
    public NewsItemDTO save(NewsItemDTO newsItemDTO) {
        LOG.debug("Request to save NewsItem : {}", newsItemDTO);
        NewsItem newsItem = newsItemMapper.toEntity(newsItemDTO);
        newsItem = newsItemRepository.save(newsItem);
        return newsItemMapper.toDto(newsItem);
    }

    @CacheEvict(cacheNames = "newsItemsPages", allEntries = true)
    public NewsItemDTO update(NewsItemDTO newsItemDTO) {
        LOG.debug("Request to update NewsItem : {}", newsItemDTO);
        NewsItem newsItem = newsItemMapper.toEntity(newsItemDTO);
        newsItem = newsItemRepository.save(newsItem);
        return newsItemMapper.toDto(newsItem);
    }

    @CacheEvict(cacheNames = "newsItemsPages", allEntries = true)
    public Optional<NewsItemDTO> partialUpdate(NewsItemDTO newsItemDTO) {
        LOG.debug("Request to partially update NewsItem : {}", newsItemDTO);

        return newsItemRepository
            .findById(newsItemDTO.getId())
            .map(existingNewsItem -> {
                newsItemMapper.partialUpdate(existingNewsItem, newsItemDTO);
                return existingNewsItem;
            })
            .map(newsItemRepository::save)
            .map(newsItemMapper::toDto);
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
        return newsItemRepository.findOneWithEagerRelationships(id).map(newsItemMapper::toDto);
    }

    @CacheEvict(cacheNames = "newsItemsPages", allEntries = true)
    public void delete(Long id) {
        LOG.debug("Request to delete NewsItem : {}", id);
        newsItemRepository.deleteById(id);
    }
}
