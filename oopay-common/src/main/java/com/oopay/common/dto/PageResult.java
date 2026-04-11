package com.oopay.common.dto;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 分页结果封装
 */
@Data
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private long total;
    private long pages;
    private long current;
    private long size;
    private List<T> records;

    public PageResult() {
    }

    public PageResult(long total, long pages, long current, long size, List<T> records) {
        this.total = total;
        this.pages = pages;
        this.current = current;
        this.size = size;
        this.records = records;
    }

    public static <T> PageResult<T> of(IPage<T> page) {
        return new PageResult<>(
                page.getTotal(),
                page.getPages(),
                page.getCurrent(),
                page.getSize(),
                page.getRecords()
        );
    }
}
