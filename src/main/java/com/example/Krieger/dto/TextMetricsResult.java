package com.example.Krieger.dto;

public class TextMetricsResult {
    private final Long id;
    private final int words;
    private final int characters;
    private final int lines;
    private final int readingTimeSeconds;

    public TextMetricsResult(Long id, int words, int characters, int lines, int readingTimeSeconds) {
        this.id = id;
        this.words = words;
        this.characters = characters;
        this.lines = lines;
        this.readingTimeSeconds = readingTimeSeconds;
    }

    public Long getId() { return id; }
    public int getWords() { return words; }
    public int getCharacters() { return characters; }
    public int getLines() { return lines; }
    public int getReadingTimeSeconds() { return readingTimeSeconds; }
}
