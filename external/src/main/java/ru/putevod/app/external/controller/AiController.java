package ru.putevod.app.external.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.putevod.app.external.dto.ai.PackingListRequest;
import ru.putevod.app.external.dto.ai.PackingListResponse;
import ru.putevod.app.external.dto.ai.PackingListTemplateContent;
import ru.putevod.app.external.dto.ai.PackingListTemplatesResponse;
import ru.putevod.app.external.security.CurrentUser;
import ru.putevod.app.external.service.AiService;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    @PostMapping("/packing-list")
    public ResponseEntity<PackingListResponse> generatePackingList(
            @RequestBody PackingListRequest request,
            @CurrentUser(info = true) Map<String, Object> userInfo) {
        return ResponseEntity.ok(aiService.generatePackingList(request));
    }

    @GetMapping("/packing-list/templates")
    public ResponseEntity<PackingListTemplatesResponse> getPackingListTemplates(
            @CurrentUser(info = true) Map<String, Object> userInfo) {
        return ResponseEntity.ok(aiService.getPackingListTemplates());
    }

    @GetMapping("/packing-list/template/{templateId}")
    public ResponseEntity<PackingListTemplateContent> getPackingListTemplateContent(
            @PathVariable String templateId,
            @CurrentUser(info = true) Map<String, Object> userInfo) {
        return ResponseEntity.ok(aiService.getPackingListTemplateContent(templateId));
    }
} 