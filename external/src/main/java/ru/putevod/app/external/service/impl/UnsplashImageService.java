package ru.putevod.app.external.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.putevod.app.external.dto.response.UnsplashResponse;
import ru.putevod.app.external.service.ImageService;

@Service
@RequiredArgsConstructor
@Slf4j
public class UnsplashImageService implements ImageService {

    private final RestTemplate restTemplate;

    @Value("${unsplash.api.key:YOUR_UNSPLASH_ACCESS_KEY}")
    private String accessKey;

    @Value("${unsplash.api.url:https://api.unsplash.com}")
    private String apiUrl;

    @Override
    public UnsplashResponse getCityImages(String city) {
        if (city == null || city.trim().isEmpty()) {
            log.warn("Город не указан для поиска изображений");
            return null;
        }

        try {
            String searchQuery = city.trim() + " city landmarks architecture";

            String url = UriComponentsBuilder.fromHttpUrl(apiUrl + "/search/photos")
                    .queryParam("query", searchQuery)
                    .queryParam("page", 1)
                    .queryParam("per_page", 6)
                    .queryParam("orientation", "landscape")
                    .queryParam("content_filter", "high")
                    .queryParam("order_by", "relevant")
                    .build()
                    .toUriString();

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Client-ID " + accessKey);
            headers.set("Accept-Version", "v1");

            HttpEntity<String> entity = new HttpEntity<>(headers);

            log.debug("Запрос к Unsplash API: {}", url);

            ResponseEntity<UnsplashResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    UnsplashResponse.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                UnsplashResponse responseBody = response.getBody();
                log.info("Получены изображения для города {}: {} результатов", city,
                        responseBody.getResults() != null ? responseBody.getResults().size() : 0);
                return responseBody;
            } else {
                log.warn("Unsplash API вернул ошибку для города {}: {}", city, response.getStatusCode());
                return null;
            }
        } catch (RestClientException e) {
            log.error("Ошибка при запросе к Unsplash API для города {}: {}", city, e.getMessage());
            return null;
        } catch (Exception e) {
            log.error("Неожиданная ошибка при запросе изображений для города {}: {}", city, e.getMessage(), e);
            return null;
        }
    }
} 