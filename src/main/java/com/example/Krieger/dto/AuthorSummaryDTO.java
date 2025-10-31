package com.example.Krieger.dto;

public class AuthorSummaryDTO {
    private Long id; private String firstName; private String lastName;
    public AuthorSummaryDTO() {}
    public AuthorSummaryDTO(Long id, String firstName, String lastName) {
        this.id = id; this.firstName = firstName; this.lastName = lastName;
    }
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getFirstName() { return firstName; } public void setFirstName(String v){ this.firstName=v; }
    public String getLastName() { return lastName; } public void setLastName(String v){ this.lastName=v; }
}
