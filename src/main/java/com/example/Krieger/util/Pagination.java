package com.example.Krieger.util;


import com.example.Krieger.exception.InvalidPaginationException;

/** Safe parsing & bounds for pagination params. */
public final class Pagination {
    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;

    private Pagination() {}

    public static int safePage(String pageParam) {
        if (pageParam == null || pageParam.isBlank()) return DEFAULT_PAGE;
        final int p;
        try {
            p = Integer.parseInt(pageParam);
        } catch (NumberFormatException e) {
            throw new InvalidPaginationException("page must be an integer >= 0");
        }
        if (p < 0) {
            throw new InvalidPaginationException("page must be >= 0");
        }
        return p;
    }

    public static int safeSize(String sizeParam) {
        if (sizeParam == null || sizeParam.isBlank()) return DEFAULT_SIZE;
        final int s;
        try {
            s = Integer.parseInt(sizeParam);
        } catch (NumberFormatException e) {
            throw new InvalidPaginationException("size must be an integer between 1 and " + MAX_PAGE_SIZE);
        }
        if (s < 1 || s > MAX_PAGE_SIZE) {
            throw new InvalidPaginationException("size must be between 1 and " + MAX_PAGE_SIZE);
        }
        return s;
    }
}
