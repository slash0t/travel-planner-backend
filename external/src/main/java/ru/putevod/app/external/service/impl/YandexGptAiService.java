package ru.putevod.app.external.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import ru.putevod.app.external.dto.ai.*;
import ru.putevod.app.external.dto.ai.YandexGptRequest;
import ru.putevod.app.external.exception.ServiceUnavailableException;
import ru.putevod.app.external.service.AiService;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Primary
@Slf4j
public class YandexGptAiService implements AiService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    @Value("${app.yandex.gpt.api-key:}")
    private String apiKey;
    
    @Value("${app.yandex.gpt.folder-id:}")
    private String folderId;
    
    @Value("${app.yandex.gpt.api-url:https://llm.api.cloud.yandex.net/foundationModels/v1/completion}")
    private String apiUrl;
    
    @Value("${app.yandex.gpt.max-tokens:1000}")
    private String maxTokens;
    
    @Value("${app.yandex.gpt.temperature:0.7}")
    private Double temperature;

    @Override
    public PackingListResponse generatePackingList(PackingListRequest request) {
        log.info("Генерация списка вещей для путешествия с YandexGPT: {}", request);

        try {
            String systemPrompt = buildPackingListSystemPrompt();
            String userPrompt = buildPackingListUserPrompt(request);
            
            String response = callYandexGptApi(systemPrompt, userPrompt);
            List<PackingCategoryDto> categories = parsePackingListResponse(response);
            
            if (categories.isEmpty()) {
                log.warn("YandexGPT вернул пустой список");
                return createFallbackPackingList();
            }
            
            int totalItems = categories.stream()
                    .mapToInt(category -> category.getItems().size())
                    .sum();
            
            return PackingListResponse.builder()
                    .categories(categories)
                    .suggestions(List.of(
                        "Проверьте погоду в пункте назначения перед упаковкой",
                        "Сделайте фотокопии важных документов",
                        "Уточните требования авиакомпании к багажу"
                    ))
                    .totalItems(totalItems)
                    .build();
                    
        } catch (Exception e) {
            log.error("Ошибка при генерации списка с помощью YandexGPT: {}", e.getMessage(), e);
            return createFallbackPackingList();
        }
    }

    @Override
    public PackingListTemplatesResponse getPackingListTemplates() {
        log.info("Получение списка шаблонов (заглушка)");

        List<PackingListTemplate> templates = List.of(
            PackingListTemplate.builder()
                .id("beach-trip")
                .name("Пляжный отдых")
                .description("Базовый список вещей для пляжного отпуска")
                .category("Пляжный отдых")
                .imageUrl("https://example.com/images/beach.jpg")
                .itemCount(20)
                .build(),
            PackingListTemplate.builder()
                .id("city-trip")
                .name("Городской туризм")
                .description("Необходимые вещи для исследования городов")
                .category("Городской туризм")
                .imageUrl("https://example.com/images/city.jpg")
                .itemCount(18)
                .build(),
            PackingListTemplate.builder()
                .id("hiking")
                .name("Поход в горы")
                .description("Список для любителей активного отдыха в горах")
                .category("Активный отдых")
                .imageUrl("https://example.com/images/hiking.jpg")
                .itemCount(25)
                .build()
        );
        
        return PackingListTemplatesResponse.builder()
                .templates(templates)
                .build();
    }

    @Override
    public PackingListTemplateContent getPackingListTemplateContent(String templateId) {
        log.info("Получение содержимого шаблона (заглушка) с ID: {}", templateId);

        return PackingListTemplateContent.builder()
                .id(templateId)
                .name("Базовый шаблон для поездки")
                .description("Стандартный набор вещей для любой поездки")
                .categories(createSampleCategories())
                .totalItems(12)
                .build();
    }
    
    private String buildPackingListSystemPrompt() {
        return "Ты - эксперт по планированию путешествий. Твоя задача - создать детальный список вещей для поездки, " +
               "разделенный по категориям. Отвечай в формате:\n" +
               "КАТЕГОРИЯ1:\n" +
               "- предмет 1\n" +
               "- предмет 2\n" +
               "КАТЕГОРИЯ2:\n" +
               "- предмет 3\n" +
               "- предмет 4\n" +
               "Используй категории: Документы, Одежда, Гигиена, Техника, Аптечка, Другое";
    }
    
    private String buildPackingListUserPrompt(PackingListRequest request) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Составь список вещей для поездки со следующими параметрами:\n");
        
        if (request.getDestination() != null && !request.getDestination().trim().isEmpty()) {
            prompt.append("Место назначения: ").append(request.getDestination()).append("\n");
        }
        
        if (request.getDuration() != null) {
            prompt.append("Продолжительность: ").append(request.getDuration()).append(" дней\n");
        }
        
        if (request.getTravelType() != null && !request.getTravelType().trim().isEmpty()) {
            prompt.append("Тип поездки: ").append(request.getTravelType()).append("\n");
        }
        
        if (request.getSeason() != null && !request.getSeason().trim().isEmpty()) {
            prompt.append("Сезон: ").append(request.getSeason()).append("\n");
        }
        
        if (request.getActivities() != null && !request.getActivities().isEmpty()) {
            prompt.append("Планируемые активности: ").append(String.join(", ", request.getActivities())).append("\n");
        }
        
        if (request.getParticipants() != null && !request.getParticipants().isEmpty()) {
            prompt.append("Участники: ");
            for (ParticipantDto participant : request.getParticipants()) {
                prompt.append(participant.getType()).append(" (").append(participant.getCount()).append(" чел.), ");
            }
            prompt.append("\n");
        }
        
        if (request.getTransportation() != null && !request.getTransportation().isEmpty()) {
            prompt.append("Транспорт: ").append(String.join(", ", request.getTransportation())).append("\n");
        }
        
        if (request.getAccommodation() != null && !request.getAccommodation().trim().isEmpty()) {
            prompt.append("Размещение: ").append(request.getAccommodation()).append("\n");
        }
        
        if (request.getAdditionalInfo() != null && !request.getAdditionalInfo().trim().isEmpty()) {
            prompt.append("Дополнительная информация: ").append(request.getAdditionalInfo()).append("\n");
        }
        
        return prompt.toString();
    }
    
    private String callYandexGptApi(String systemPrompt, String userPrompt) throws Exception {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new ServiceUnavailableException("API ключ для YandexGPT не настроен");
        }
        
        if (folderId == null || folderId.isEmpty()) {
            throw new ServiceUnavailableException("Folder ID для YandexGPT не настроен");
        }
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Api-Key " + apiKey);
        headers.set("x-folder-id", folderId);
        
        List<YandexGptRequest.Message> messages = new ArrayList<>();
        messages.add(YandexGptRequest.Message.builder()
                .role("system")
                .text(systemPrompt)
                .build());
        messages.add(YandexGptRequest.Message.builder()
                .role("user")
                .text(userPrompt)
                .build());
        
        String modelUri = String.format("gpt://%s/yandexgpt/latest", folderId);
        
        YandexGptRequest requestBody = YandexGptRequest.builder()
                .modelUri(modelUri)
                .completionOptions(YandexGptRequest.CompletionOptions.builder()
                        .stream(false)
                        .temperature(temperature)
                        .maxTokens(maxTokens)
                        .build())
                .messages(messages)
                .build();
        
        HttpEntity<YandexGptRequest> requestEntity = new HttpEntity<>(requestBody, headers);
        
        try {
            String responseJson = restTemplate.postForObject(apiUrl, requestEntity, String.class);
            
            if (responseJson == null || responseJson.isEmpty()) {
                throw new ServiceUnavailableException("Пустой ответ от API YandexGPT");
            }
            
            JsonNode rootNode = objectMapper.readTree(responseJson);
            JsonNode resultNode = rootNode.path("result");
            
            if (resultNode.isMissingNode()) {
                throw new ServiceUnavailableException("Отсутствуют результаты в ответе от API YandexGPT");
            }
            
            JsonNode alternativesNode = resultNode.path("alternatives");
            
            if (alternativesNode.isMissingNode() || alternativesNode.isEmpty()) {
                throw new ServiceUnavailableException("Отсутствуют альтернативы в ответе от API YandexGPT");
            }
            
            return alternativesNode.path(0).path("message").path("text").asText();
        } catch (RestClientException e) {
            log.error("Ошибка при вызове YandexGPT API: {}", e.getMessage(), e);
            throw new ServiceUnavailableException("Ошибка при вызове AI API: " + e.getMessage(), e);
        }
    }
    
    private List<PackingCategoryDto> parsePackingListResponse(String aiResponse) {
        if (aiResponse == null || aiResponse.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        List<PackingCategoryDto> categories = new ArrayList<>();
        String[] lines = aiResponse.split("\\r?\\n");
        
        String currentCategory = null;
        List<PackingItemDto> currentItems = new ArrayList<>();
        
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;
            
            if (line.endsWith(":")) {
                if (currentCategory != null && !currentItems.isEmpty()) {
                    categories.add(PackingCategoryDto.builder()
                            .name(currentCategory)
                            .items(new ArrayList<>(currentItems))
                            .build());
                }

                currentCategory = line.substring(0, line.length() - 1);
                currentItems.clear();
            } else if (line.startsWith("-") && currentCategory != null) {
                String itemName = line.substring(1).trim();
                if (!itemName.isEmpty()) {
                    currentItems.add(PackingItemDto.builder()
                            .name(itemName)
                            .description("")
                            .priority("recommended")
                            .quantity(1)
                            .build());
                }
            }
        }

        if (currentCategory != null && !currentItems.isEmpty()) {
            categories.add(PackingCategoryDto.builder()
                    .name(currentCategory)
                    .items(currentItems)
                    .build());
        }
        
        return categories;
    }
    
    private PackingListResponse createFallbackPackingList() {
        return PackingListResponse.builder()
                .categories(createSampleCategories())
                .suggestions(List.of(
                    "Не удалось сгенерировать персонализированный список",
                    "Используется базовый шаблон",
                    "Попробуйте повторить запрос позже"
                ))
                .totalItems(12)
                .build();
    }
    
    private List<PackingCategoryDto> createSampleCategories() {
        List<PackingCategoryDto> categories = new ArrayList<>();

        List<PackingItemDto> documents = List.of(
            PackingItemDto.builder()
                .name("Паспорт")
                .description("Внутренний или заграничный паспорт")
                .priority("essential")
                .quantity(1)
                .build(),
            PackingItemDto.builder()
                .name("Деньги и банковские карты")
                .description("Наличные и банковские карты")
                .priority("essential")
                .quantity(1)
                .build(),
            PackingItemDto.builder()
                .name("Билеты")
                .description("Распечатки билетов или электронные билеты")
                .priority("essential")
                .quantity(1)
                .build()
        );
        
        categories.add(PackingCategoryDto.builder()
                .name("Документы")
                .items(documents)
                .build());

        List<PackingItemDto> clothes = List.of(
            PackingItemDto.builder()
                .name("Футболки")
                .description("Футболки на каждый день")
                .priority("essential")
                .quantity(5)
                .build(),
            PackingItemDto.builder()
                .name("Брюки/шорты")
                .description("По погоде")
                .priority("essential")
                .quantity(2)
                .build(),
            PackingItemDto.builder()
                .name("Нижнее белье")
                .description("На каждый день + запасное")
                .priority("essential")
                .quantity(6)
                .build(),
            PackingItemDto.builder()
                .name("Носки")
                .description("На каждый день + запасные")
                .priority("essential")
                .quantity(6)
                .build()
        );
        
        categories.add(PackingCategoryDto.builder()
                .name("Одежда")
                .items(clothes)
                .build());

        List<PackingItemDto> hygiene = List.of(
            PackingItemDto.builder()
                .name("Зубная щетка и паста")
                .description("")
                .priority("essential")
                .quantity(1)
                .build(),
            PackingItemDto.builder()
                .name("Шампунь")
                .description("В мини-флаконе")
                .priority("recommended")
                .quantity(1)
                .build(),
            PackingItemDto.builder()
                .name("Полотенце")
                .description("Дорожное полотенце")
                .priority("recommended")
                .quantity(1)
                .build(),
            PackingItemDto.builder()
                .name("Солнцезащитный крем")
                .description("SPF 30+")
                .priority("recommended")
                .quantity(1)
                .build(),
            PackingItemDto.builder()
                .name("Лекарства")
                .description("Необходимые лекарства и аптечка")
                .priority("essential")
                .quantity(1)
                .build()
        );
        
        categories.add(PackingCategoryDto.builder()
                .name("Гигиена")
                .items(hygiene)
                .build());
        
        return categories;
    }
} 