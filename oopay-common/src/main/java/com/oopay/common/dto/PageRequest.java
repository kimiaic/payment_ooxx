package com.oopay.common.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 分页请求参数
 */
@Data
public class PageRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 默认页码
     */
    private static final int DEFAULT_PAGE_NUM = 1;

    /**
     * 默认每页大小
     */
    private static final int DEFAULT_PAGE_SIZE = 10;

    /**
     * 最大每页大小
     */
    private static final int MAX_PAGE_SIZE = 1000;

    /**
     * 当前页码（从1开始）
     */
    private Integer pageNum;

    /**
     * 每页大小
     */
    private Integer pageSize;

    /**
     * 排序字段
     */
    private String orderBy;

    /**
     * 是否升序（true升序，false降序）
     */
    private Boolean asc;

    public PageRequest() {
        this.pageNum = DEFAULT_PAGE_NUM;
        this.pageSize = DEFAULT_PAGE_SIZE;
        this.asc = false;
    }

    /**
     * 获取有效的页码
     */
    public Integer getPageNum() {
        if (pageNum == null || pageNum < 1) {
            return DEFAULT_PAGE_NUM;
        }
        return pageNum;
    }

    /**
     * 获取有效的每页大小
     */
    public Integer getPageSize() {
        if (pageSize == null || pageSize < 1) {
            return DEFAULT_PAGE_SIZE;
        }
        if (pageSize > MAX_PAGE_SIZE) {
            return MAX_PAGE_SIZE;
        }
        return pageSize;
    }

    /**
     * 计算偏移量（用于SQL查询）
     */
    public Integer getOffset() {
        return (getPageNum() - 1) * getPageSize();
    }

    /**
     * 构建分页请求
     */
    public static PageRequest of(Integer pageNum, Integer pageSize) {
        PageRequest request = new PageRequest();
        request.setPageNum(pageNum);
        request.setPageSize(pageSize);
        return request;
    }

    /**
     * 构建带排序的分页请求
     */
    public static PageRequest of(Integer pageNum, Integer pageSize, String orderBy, Boolean asc) {
        PageRequest request = new PageRequest();
        request.setPageNum(pageNum);
        request.setPageSize(pageSize);
        request.setOrderBy(orderBy);
        request.setAsc(asc);
        return request;
    }
}
