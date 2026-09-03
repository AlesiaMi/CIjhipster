package com.mycompany.myapp.service.dto;

import java.util.List;

public class DataSourcePageCacheDTO {

    private List<DataSourceDTO> content;
    private int pageNumber;
    private int pageSize;
    private long totalElements;

    public DataSourcePageCacheDTO() {}

    public DataSourcePageCacheDTO(List<DataSourceDTO> content, int pageNumber, int pageSize, long totalElements) {
        this.content = content;
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.totalElements = totalElements;
    }

    public List<DataSourceDTO> getContent() {
        return content;
    }

    public void setContent(List<DataSourceDTO> content) {
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
