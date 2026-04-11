package com.oopay.common.dto;

import lombok.Data;

@Data
public class PageRequest {
    private static final long DEFAULT_CURRENT = 1L;
    private static final long DEFAULT_SIZE = 20L;
    private static final long MAX_SIZE = 100L;

    private Long current;
    private Long size;

    public Long getCurrent() {
        if (current == null || current < 1) {
            return DEFAULT_CURRENT;
        }
        return current;
    }

    public Long getSize() {
        if (size == null || size < 1) {
            return DEFAULT_SIZE;
        }
        if (size > MAX_SIZE) {
            return MAX_SIZE;
        }
        return size;
    }
}
