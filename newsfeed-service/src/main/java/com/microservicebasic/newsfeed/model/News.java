package com.microservicebasic.newsfeed.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Document("news")
public class News {
    @Id
    private String id;
    private String title;
    private String content;
    private Instant createdAt;
}
