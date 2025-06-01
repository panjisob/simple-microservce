package com.microservicebasic.newsfeed.service;

import com.microservicebasic.newsfeed.model.News;
import com.microservicebasic.newsfeed.repository.NewsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class NewsService {
    private final NewsRepository newsRepository;

    public Flux<News> getNewsPage(Instant cursor, int limit) {
        if (cursor == null) {
            cursor = Instant.now();
        }
        return newsRepository.findByCreatedAtLessThanOrderByCreatedAtDesc(cursor, PageRequest.of(0, limit));
    }

    public Mono<News> createNews(News news) {
        news.setCreatedAt(Instant.now());
        return newsRepository.save(news);
    }

    public Mono<News> getById(String id) {
        return newsRepository.findById(id);
    }

    public Mono<News> updateNews(String id, News updatedNews) {
        return newsRepository.findById(id)
                .flatMap(existing -> {
                    existing.setTitle(updatedNews.getTitle());
                    existing.setContent(updatedNews.getContent());
                    return newsRepository.save(existing);
                });
    }

    public Mono<Void> deleteNews(String id) {
        return newsRepository.deleteById(id);
    }
}
