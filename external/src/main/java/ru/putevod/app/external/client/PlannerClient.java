package ru.putevod.app.external.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import ru.putevod.app.external.exception.ServiceUnavailableException;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class PlannerClient {

    private final RestTemplate restTemplate;
    
    @Value("${app.services.planner-url:http://localhost:8082}")
    private String plannerServiceUrl;
    
    public Map<String, Object> getTripDetails(Long tripId) {
        String url = plannerServiceUrl + "/api/v1/trips/" + tripId;

        HttpEntity<?> entity = createHttpEntityWithAuthHeader();
        
        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody();
            } else {
                log.warn("Получен пустой ответ от сервиса планировщика при запросе поездки с ID {}", tripId);
                return Collections.emptyMap();
            }
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                log.error("Ошибка авторизации при получении информации о поездке с ID {}: {}", tripId, e.getMessage());
                throw new ServiceUnavailableException("Ошибка авторизации при получении информации о поездке: " + e.getMessage());
            } else if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                log.warn("Поездка с ID {} не найдена", tripId);
                return Collections.emptyMap();
            } else {
                log.error("Ошибка при получении информации о поездке с ID {}: {}", tripId, e.getMessage(), e);
                throw new ServiceUnavailableException("Ошибка при получении информации о поездке: " + e.getMessage());
            }
        } catch (ResourceAccessException e) {
            log.error("Сервис планировщика недоступен: {}", e.getMessage(), e);
            throw new ServiceUnavailableException("Сервис планировщика недоступен. Пожалуйста, повторите попытку позже.");
        } catch (RestClientException e) {
            log.error("Ошибка при получении информации о поездке с ID {}: {}", tripId, e.getMessage(), e);
            throw new ServiceUnavailableException("Ошибка при получении информации о поездке: " + e.getMessage());
        }
    }
    
    public Map<String, Object> getTemplateDetails(Long templateId) {
        String url = plannerServiceUrl + "/api/v1/templates/" + templateId;

        HttpEntity<?> entity = createHttpEntityWithAuthHeader();
        
        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody();
            } else {
                log.warn("Получен пустой ответ от сервиса планировщика при запросе шаблона с ID {}", templateId);
                return Collections.emptyMap();
            }
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                log.error("Ошибка авторизации при получении информации о шаблоне с ID {}: {}", templateId, e.getMessage());
                throw new ServiceUnavailableException("Ошибка авторизации при получении информации о шаблоне: " + e.getMessage());
            } else if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                log.warn("Шаблон с ID {} не найден", templateId);
                return Collections.emptyMap();
            } else {
                log.error("Ошибка при получении информации о шаблоне с ID {}: {}", templateId, e.getMessage(), e);
                throw new ServiceUnavailableException("Ошибка при получении информации о шаблоне: " + e.getMessage());
            }
        } catch (ResourceAccessException e) {
            log.error("Сервис планировщика недоступен: {}", e.getMessage(), e);
            throw new ServiceUnavailableException("Сервис планировщика недоступен. Пожалуйста, повторите попытку позже.");
        } catch (RestClientException e) {
            log.error("Ошибка при получении информации о шаблоне с ID {}: {}", templateId, e.getMessage(), e);
            throw new ServiceUnavailableException("Ошибка при получении информации о шаблоне: " + e.getMessage());
        }
    }
    
    public List<String> getTemplateItems(Long templateId) {
        String url = plannerServiceUrl + "/api/v1/templates/" + templateId + "/items";
        
        // Создаем HttpEntity с заголовками авторизации
        HttpEntity<?> entity = createHttpEntityWithAuthHeader();
        
        try {
            ResponseEntity<List<String>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<List<String>>() {}
            );
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody();
            } else {
                log.warn("Получен пустой список элементов от сервиса планировщика для шаблона с ID {}", templateId);
                return Collections.emptyList();
            }
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                log.error("Ошибка авторизации при получении элементов шаблона с ID {}: {}", templateId, e.getMessage());
                throw new ServiceUnavailableException("Ошибка авторизации при получении элементов шаблона: " + e.getMessage());
            } else if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                log.warn("Шаблон с ID {} не найден или не содержит элементов", templateId);
                return Collections.emptyList();
            } else {
                log.error("Ошибка при получении элементов шаблона с ID {}: {}", templateId, e.getMessage(), e);
                throw new ServiceUnavailableException("Ошибка при получении элементов шаблона: " + e.getMessage());
            }
        } catch (ResourceAccessException e) {
            log.error("Сервис планировщика недоступен: {}", e.getMessage(), e);
            throw new ServiceUnavailableException("Сервис планировщика недоступен. Пожалуйста, повторите попытку позже.");
        } catch (RestClientException e) {
            log.error("Ошибка при получении элементов шаблона с ID {}: {}", templateId, e.getMessage(), e);
            throw new ServiceUnavailableException("Ошибка при получении элементов шаблона: " + e.getMessage());
        }
    }
    
    /**
     * Создает HttpEntity с заголовками авторизации из текущего запроса
     */
    private HttpEntity<?> createHttpEntityWithAuthHeader() {
        HttpHeaders headers = new HttpHeaders();

        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

                if (authHeader != null && !authHeader.isEmpty()) {
                    if (authHeader.startsWith("Bearer ")) {
                        log.debug("Передача JWT токена в запрос к сервису Planner ");
                        headers.set(HttpHeaders.AUTHORIZATION, authHeader);
                    } else {
                        log.debug("Передача JWT токена в запрос к сервису Planner (добавлен префикс Bearer)");
                        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + authHeader);
                    }
                } else {
                    log.warn("Заголовок авторизации отсутствует в текущем запросе");
                }
            } else {
                log.warn("Не удалось получить текущий запрос для извлечения заголовка авторизации");
            }
        } catch (Exception e) {
            log.error("Ошибка при получении заголовка авторизации: {}", e.getMessage(), e);
        }

        return new HttpEntity<>(headers);
    }
}