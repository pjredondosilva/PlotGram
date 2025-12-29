package es.plotgram.backend.tmdb.configuracion;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class TmdbConfig {

    @Bean
    WebClient tmdbWebClient(
            @Value("${tmdb.base-url}") String baseUrl,
            @Value("${tmdb.token}") String token
    ) {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + token)
                .defaultHeader("Accept", "application/json")
                .build();
    }
}
