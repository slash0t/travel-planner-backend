package ru.putevod.app.planner.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.putevod.app.planner.dto.TodoTemplateDto;
import ru.putevod.app.planner.model.TemplateItem;
import ru.putevod.app.planner.model.TodoTemplate;
import ru.putevod.app.planner.model.User;
import ru.putevod.app.planner.repository.TemplateItemRepository;
import ru.putevod.app.planner.repository.TodoTemplateRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TodoTemplateControllerTest {

    @Mock
    private TodoTemplateRepository todoTemplateRepository;

    @Mock
    private TemplateItemRepository templateItemRepository;

    @InjectMocks
    private TodoTemplateController todoTemplateController;

    private TodoTemplate mockTemplate;
    private TemplateItem mockItem;
    private User mockUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        mockUser = new User();
        mockUser.setUserId(1L);

        mockTemplate = TodoTemplate.builder()
                .templateId(1L)
                .title("Test Template")
                .description("Test Description")
                .category("business")
                .isSystem(false)
                .createdBy(mockUser)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        mockItem = TemplateItem.builder()
                .itemId(1L)
                .template(mockTemplate)
                .content("Test Item")
                .orderPosition(1)
                .createdAt(LocalDateTime.now())
                .build();

        mockTemplate.setItems(Arrays.asList(mockItem));
    }

    @Test
    void getAllTemplates_ShouldReturnTemplates() {
        when(todoTemplateRepository.findAll()).thenReturn(Arrays.asList(mockTemplate));

        ResponseEntity<List<TodoTemplateDto>> response = todoTemplateController.getAllTemplates();

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(todoTemplateRepository).findAll();
    }

    @Test
    void getAllTemplates_ShouldReturnEmptyList() {
        when(todoTemplateRepository.findAll()).thenReturn(List.of());

        ResponseEntity<List<TodoTemplateDto>> response = todoTemplateController.getAllTemplates();

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().size());
        verify(todoTemplateRepository).findAll();
    }

    @Test
    void getTemplateDetails_ShouldReturnTemplate() {
        when(todoTemplateRepository.findById(1L)).thenReturn(Optional.of(mockTemplate));

        ResponseEntity<TodoTemplateDto> response = todoTemplateController.getTemplateDetails(1L);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(mockTemplate.getTemplateId(), response.getBody().getTemplateId());
        assertEquals(mockTemplate.getTitle(), response.getBody().getTitle());
        assertEquals(mockTemplate.getDescription(), response.getBody().getDescription());
        assertEquals(mockTemplate.getCategory(), response.getBody().getCategory());
        verify(todoTemplateRepository).findById(1L);
    }

    @Test
    void getTemplateDetails_ShouldReturnNotFound() {
        when(todoTemplateRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseEntity<TodoTemplateDto> response = todoTemplateController.getTemplateDetails(1L);

        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(todoTemplateRepository).findById(1L);
    }

    @Test
    void getTemplateItems_ShouldReturnItems() {
        when(todoTemplateRepository.findById(1L)).thenReturn(Optional.of(mockTemplate));
        when(templateItemRepository.findByTemplateOrderByOrderPosition(mockTemplate))
                .thenReturn(Arrays.asList(mockItem));

        ResponseEntity<List<String>> response = todoTemplateController.getTemplateItems(1L);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(mockItem.getContent(), response.getBody().get(0));
        verify(todoTemplateRepository).findById(1L);
        verify(templateItemRepository).findByTemplateOrderByOrderPosition(mockTemplate);
    }

    @Test
    void getTemplateItems_ShouldReturnNotFound() {
        when(todoTemplateRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseEntity<List<String>> response = todoTemplateController.getTemplateItems(1L);

        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(todoTemplateRepository).findById(1L);
    }

    @Test
    void getTemplatesByCategory_ShouldReturnTemplates() {
        when(todoTemplateRepository.findByCategory("business"))
                .thenReturn(Arrays.asList(mockTemplate));

        ResponseEntity<List<TodoTemplateDto>> response = todoTemplateController.getTemplatesByCategory("business");

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("business", response.getBody().get(0).getCategory());
        verify(todoTemplateRepository).findByCategory("business");
    }

    @Test
    void getTemplatesByCategory_ShouldReturnEmptyList() {
        when(todoTemplateRepository.findByCategory("business")).thenReturn(List.of());

        ResponseEntity<List<TodoTemplateDto>> response = todoTemplateController.getTemplatesByCategory("business");

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().size());
        verify(todoTemplateRepository).findByCategory("business");
    }
} 