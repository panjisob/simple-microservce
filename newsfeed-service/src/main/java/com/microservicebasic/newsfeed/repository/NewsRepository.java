package com.microservicebasic.newsfeed.repository;

import com.microservicebasic.newsfeed.model.News;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

import java.time.Instant;

public interface NewsRepository extends ReactiveMongoRepository<News, String> {
    Flux<News> findByCreatedAtLessThanOrderByCreatedAtDesc(Instant cursor, Pageable pageable);
}
