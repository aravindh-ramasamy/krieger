package com.example.krieger.controller;

import com.example.Krieger.controller.DocumentController;
import com.example.Krieger.entity.Document;
import com.example.Krieger.service.DocumentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DocumentControllerPaginationHeadersTest {

    @Mock
    private DocumentService documentService;

    @InjectMocks
    private DocumentController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void middlePage_includesFirstPrevNextLast_andTotalCount() throws Exception {
        // page=2, size=20, totalElements=137 -> totalPages = 7 (0..6)
        Pageable pageable = PageRequest.of(2, 20, Sort.by(Sort.Direction.ASC, "title"));
        Page<Document> page = new PageImpl<>(List.of(new Document()), pageable, 137);

        when(documentService.searchDocuments(eq(42L), eq("foo"), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/documents")
                        .param("authorId", "42")
                        .param("q", "foo")
                        .param("page", "2")
                        .param("size", "20")
                        .param("sort", "title,asc"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Total-Count", "137"))
                // first (order-agnostic assertions)
                .andExpect(header().string(HttpHeaders.LINK, allOf(
                        containsString("rel=\"first\""),
                        containsString("page=0&size=20"),
                        containsString("sort=title,asc")
                )))
                // prev (1)
                .andExpect(header().string(HttpHeaders.LINK, allOf(
                        containsString("rel=\"prev\""),
                        containsString("page=1&size=20"),
                        containsString("sort=title,asc")
                )))
                // next (3)
                .andExpect(header().string(HttpHeaders.LINK, allOf(
                        containsString("rel=\"next\""),
                        containsString("page=3&size=20"),
                        containsString("sort=title,asc")
                )))
                // last (6)
                .andExpect(header().string(HttpHeaders.LINK, allOf(
                        containsString("rel=\"last\""),
                        containsString("page=6&size=20"),
                        containsString("sort=title,asc")
                )));
    }

    @Test
    void firstPage_omitsPrev_includesNext() throws Exception {
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "id"));
        Page<Document> page = new PageImpl<>(List.of(), pageable, 35); // pages: 0..3

        when(documentService.searchDocuments(isNull(), isNull(), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/documents")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "id,desc"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Total-Count", "35"))
                .andExpect(header().string(HttpHeaders.LINK, containsString("rel=\"first\"")))
                .andExpect(header().string(HttpHeaders.LINK, containsString("rel=\"next\"")))
                .andExpect(header().string(HttpHeaders.LINK, containsString("rel=\"last\"")))
                .andExpect(header().string(HttpHeaders.LINK, not(containsString("rel=\"prev\""))));
    }

    @Test
    void lastPage_omitsNext_includesPrev() throws Exception {
        // totalElements=30, size=10 => pages 0..2; last is 2
        Pageable pageable = PageRequest.of(2, 10, Sort.by(Sort.Direction.DESC, "id"));
        Page<Document> page = new PageImpl<>(List.of(), pageable, 30);

        when(documentService.searchDocuments(isNull(), isNull(), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/documents")
                        .param("page", "2")
                        .param("size", "10")
                        .param("sort", "id,desc"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Total-Count", "30"))
                .andExpect(header().string(HttpHeaders.LINK, containsString("rel=\"first\"")))
                .andExpect(header().string(HttpHeaders.LINK, containsString("rel=\"prev\"")))
                .andExpect(header().string(HttpHeaders.LINK, containsString("rel=\"last\"")))
                .andExpect(header().string(HttpHeaders.LINK, not(containsString("rel=\"next\""))));
    }
}
