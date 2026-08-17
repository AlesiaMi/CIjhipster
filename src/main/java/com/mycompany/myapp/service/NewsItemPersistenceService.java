package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.NewsItem;
import com.mycompany.myapp.repository.NewsItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NewsItemPersistenceService {

    private final NewsItemRepository newsItemRepository;

    public NewsItemPersistenceService(NewsItemRepository newsItemRepository) {
        this.newsItemRepository = newsItemRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Long save(NewsItem newsItem) {
        return newsItemRepository.saveAndFlush(newsItem).getId();
    }
}
