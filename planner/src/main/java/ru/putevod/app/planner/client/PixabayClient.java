package ru.putevod.app.planner.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import ru.putevod.app.planner.dto.external.PixabayResponse;

@Component
@RequiredArgsConstructor
@Slf4j
public class PixabayClient {

    private final RestTemplate restTemplate;
    
    @Value("${pixabay.api.key:50443727-7b799271d0c5339458ce8b20d}")
    private String apiKey;
    
    @Value("${pixabay.api.url:https://pixabay.com/api/}")
    private String apiUrl;
    
    /**
     * Получает изображения достопримечательностей для указанного города
     *
     * @param city название города
     * @return ответ от API Pixabay или null в случае ошибки
     */
    public PixabayResponse getImagesForCity(String city) {
        if (city == null || city.trim().isEmpty()) {
            log.warn("Город не указан для поиска изображений");
            return null;
        }
        
        try {
            String searchTerm = city.trim() + " достопримечательность";
            String url = apiUrl + 
                    "?key=" + apiKey + 
                    "&q=" + searchTerm + 
                    "&image_type=photo" + 
                    "&orientation=horizontal" + 
                    "&order=popular" + 
                    "&per_page=3" +
                    "&safesearch=true" + 
                    "&lang=ru";
            
            log.debug("Запрос к Pixabay API: {}", url);
            
            ResponseEntity<PixabayResponse> response = restTemplate.getForEntity(url, PixabayResponse.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.info("Получены изображения для города {}: {} результатов", city, 
                        response.getBody().getHits() != null ? response.getBody().getHits().size() : 0);
                return response.getBody();
            } else {
                log.warn("Pixabay API вернул ошибку для города {}: {}", city, response.getStatusCode());
                return null;
            }
        } catch (RestClientException e) {
            log.error("Ошибка при запросе к Pixabay API для города {}: {}", city, e.getMessage());
            return null;
        }
    }
} 