package ru.putevod.app.external.service;

import ru.putevod.app.external.dto.ai.PackingListRequest;
import ru.putevod.app.external.dto.ai.PackingListResponse;
import ru.putevod.app.external.dto.ai.PackingListTemplateContent;
import ru.putevod.app.external.dto.ai.PackingListTemplatesResponse;

public interface AiService {
    /**
     * Генерация списка вещей для путешествия
     */
    PackingListResponse generatePackingList(PackingListRequest request);
    
    /**
     * Получение списка доступных шаблонов списка вещей
     */
    PackingListTemplatesResponse getPackingListTemplates();
    
    /**
     * Получение содержимого шаблона списка вещей по ID
     */
    PackingListTemplateContent getPackingListTemplateContent(String templateId);
} 