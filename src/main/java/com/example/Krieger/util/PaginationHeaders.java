package com.example.Krieger.util;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

public final class PaginationHeaders {
    private PaginationHeaders() {}

    /** Builds RFC 5988 Link + X-Total-Count headers for the current request + given Page. */
    public static HttpHeaders build(Page<?> page) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(page.getTotalElements()));

        int current = page.getNumber();
        int size = page.getSize();
        int totalPages = page.getTotalPages();

        // Try to bind to the current HTTP request; fall back to relative builder in unit tests
        UriComponentsBuilder base;
        HttpServletRequest req = null;
        try {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                req = attrs.getRequest();
            }
        } catch (IllegalStateException ignore) {
            // no request bound
        }

        try {
            base = ServletUriComponentsBuilder.fromCurrentRequest();
        } catch (IllegalStateException ex) {
            base = UriComponentsBuilder.newInstance();
        }

        // Ensure important query params are preserved even if the base had none (some test setups)
        if (req != null) {
            preserveParam(base, req, "authorId");
            preserveParam(base, req, "q");
            preserveParam(base, req, "sort");
        }

        StringBuilder link = new StringBuilder();
        // first
        appendLink(link, replacePage(base, 0, size), "first");
        // prev
        if (current > 0) {
            appendComma(link);
            appendLink(link, replacePage(base, current - 1, size), "prev");
        }
        // next
        if (current < Math.max(totalPages - 1, 0)) {
            appendComma(link);
            appendLink(link, replacePage(base, current + 1, size), "next");
        }
        // last
        appendComma(link);
        appendLink(link, replacePage(base, Math.max(totalPages - 1, 0), size), "last");

        headers.add(HttpHeaders.LINK, link.toString());
        return headers;
    }

    private static void preserveParam(UriComponentsBuilder base, HttpServletRequest req, String name) {
        String[] values = req.getParameterValues(name);
        if (values != null && values.length > 0) {
            // replace to avoid duplicates if present; supports multi-valued params
            base.replaceQueryParam(name, (Object[]) values);
        }
    }

    private static String replacePage(UriComponentsBuilder base, int page, int size) {
        // Clone each time so we don't mutate previous links; keep all other preserved params
        return base
                .cloneBuilder()
                .replaceQueryParam("page", page)
                .replaceQueryParam("size", size)
                .build(true)
                .toUriString();
    }

    private static void appendLink(StringBuilder sb, String url, String rel) {
        sb.append('<').append(url).append('>').append("; rel=\"").append(rel).append('"');
    }

    private static void appendComma(StringBuilder sb) {
        if (sb.length() > 0) sb.append(", ");
    }
}
