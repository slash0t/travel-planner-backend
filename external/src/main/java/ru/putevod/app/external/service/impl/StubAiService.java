package ru.putevod.app.external.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.putevod.app.external.dto.ai.*;
import ru.putevod.app.external.service.AiService;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class StubAiService implements AiService {

    @Override
    public PackingListResponse generatePackingList(PackingListRequest request) {
        log.info("Генерация списка вещей для путешествия (заглушка): {}", request);

        return PackingListResponse.builder()
                .categories(createSampleCategories())
                .suggestions(List.of(
                        "Не забудьте ознакомиться с погодой в пункте назначения",
                        "Проверьте наличие страховки перед поездкой",
                        "Сделайте копии документов"
                ))
                .totalItems(12)
                .build();
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