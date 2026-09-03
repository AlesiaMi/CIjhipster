package com.mycompany.myapp.service.dto;

import java.util.List;

public class NewsItemPageCacheDTO {

    private List<NewsItemDTO> content;
    private int pageNumber;
    private int pageSize;
    private long totalElements;

    public NewsItemPageCacheDTO() {}

    public NewsItemPageCacheDTO(List<NewsItemDTO> content, int pageNumber, int pageSize, long totalElements) {
        this.content = content;
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.totalElements = totalElements;
    }

    public List<NewsItemDTO> getContent() {
        return content;
    }

    public void setContent(List<NewsItemDTO> content) {
        this.content = content;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }
}
