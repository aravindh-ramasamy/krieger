package com.example.Krieger.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Document Request object that holds data
@Getter
@Setter
@NoArgsConstructor
public class DocumentDTO {
    private Long id;
    private String title;
    private String body;
    private Long authorID;
    private String reference;
}
