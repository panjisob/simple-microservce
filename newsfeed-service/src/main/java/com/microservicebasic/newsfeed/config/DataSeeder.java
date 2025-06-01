package com.microservicebasic.newsfeed.config;

import com.microservicebasic.newsfeed.model.News;
import com.microservicebasic.newsfeed.repository.NewsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final NewsRepository newsRepository;

    @Override
    public void run(String... args) {
        newsRepository.count()
                .filter(count -> count == 0)
                .flatMapMany(aLong -> {
                    return Flux.range(1, 100)
                            .map(i -> {
                                News news = new News();
                                news.setTitle("News Title #" + i);
                                news.setContent("This is the content of news item #" + i + " - " + UUID.randomUUID());
                                news.setCreatedAt(Instant.now().minusSeconds(i * 60)); // mundur 1 menit per data
                                return news;
                            });
                })
                .collectList()
                .flatMapMany(newsRepository::saveAll)
                .doOnComplete(() -> System.out.println("✅ Seeded 100 news items"))
                .subscribe();
    }
}
