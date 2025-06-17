package com.microservicebasic.newsfeed.controller;

import com.microservicebasic.newsfeed.model.News;
import com.microservicebasic.newsfeed.service.NewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1")
public class NewsController {
    private final NewsService newsService;

    @GetMapping("/all")
    public Flux<News> getNewsPage(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant cursor,
            @RequestParam(defaultValue = "10") int limit) {
        return newsService.getNewsPage(cursor, limit);
    }

    @PostMapping("/create")
    public Mono<News> createNews(@RequestBody News news) {
        return newsService.createNews(news);
    }

    @GetMapping("/{id}")
    public Mono<News> getNewsById(@PathVariable String id) {
        return newsService.getById(id);
    }

    @PutMapping("/{id}")
    public Mono<News> updateNews(@PathVariable String id, @RequestBody News news) {
        return newsService.updateNews(id, news);
    }

    @DeleteMapping("/{id}")
    public Mono<Void> deleteNews(@PathVariable String id) {
        return newsService.deleteNews(id);
    }
}
