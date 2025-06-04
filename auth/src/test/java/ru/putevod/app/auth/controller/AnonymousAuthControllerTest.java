package ru.putevod.app.auth.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import ru.putevod.app.auth.model.AnonymousUser;
import ru.putevod.app.auth.service.AnonymousUserService;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnonymousAuthControllerTest {

    @Mock
    private AnonymousUserService anonymousUserService;

    @InjectMocks
    private AnonymousAuthController anonymousAuthController;

    private MockHttpServletRequest request;
    private static final String TEST_DEVICE_ID = "test-device-id";
    private static final String TEST_USER_AGENT = "Mozilla/5.0";
    private static final String TEST_IP = "127.0.0.1";
    private static final String TEST_SCREEN_RESOLUTION = "1920x1080";

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        request.addHeader("User-Agent", TEST_USER_AGENT);
        request.setRemoteAddr(TEST_IP);
    }

    @Test
    void createAnonymousToken_WithDeviceId() {
        Map<String, Object> expectedResponse = new HashMap<>();
        expectedResponse.put("anonymousToken", "test-token");
        expectedResponse.put("expiresIn", 1800);
        expectedResponse.put("anonymousUserId", 1L);

        when(anonymousUserService.createAnonymousToken(
                eq(TEST_DEVICE_ID),
                eq(TEST_USER_AGENT),
                eq(TEST_IP)
        )).thenReturn(expectedResponse);

        ResponseEntity<Map<String, Object>> response = anonymousAuthController.createAnonymousToken(TEST_DEVICE_ID, request);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        Map<String, Object> responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(expectedResponse, responseBody);
        assertEquals(TEST_DEVICE_ID, responseBody.get("deviceId"));

        verify(anonymousUserService).createAnonymousToken(TEST_DEVICE_ID, TEST_USER_AGENT, TEST_IP);
    }

    @Test
    void createAnonymousToken_WithoutDeviceId() {
        Map<String, Object> expectedResponse = new HashMap<>();
        expectedResponse.put("anonymousToken", "test-token");
        expectedResponse.put("expiresIn", 1800);
        expectedResponse.put("anonymousUserId", 1L);

        when(anonymousUserService.createAnonymousToken(
                anyString(),
                eq(TEST_USER_AGENT),
                eq(TEST_IP)
        )).thenReturn(expectedResponse);

        ResponseEntity<Map<String, Object>> response = anonymousAuthController.createAnonymousToken(null, request);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        Map<String, Object> responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(expectedResponse, responseBody);
        assertNotNull(responseBody.get("deviceId"));
        assertDoesNotThrow(() -> UUID.fromString(responseBody.get("deviceId").toString()));

        verify(anonymousUserService).createAnonymousToken(anyString(), eq(TEST_USER_AGENT), eq(TEST_IP));
    }

    @Test
    void updateActivity() {
        ResponseEntity<Void> response = anonymousAuthController.updateActivity(TEST_DEVICE_ID);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        verify(anonymousUserService).updateLastActivity(TEST_DEVICE_ID);
    }

    @Test
    void getAnonymousStats() {
        when(anonymousUserService.getActiveAnonymousUsersCount()).thenReturn(5L);

        ResponseEntity<Map<String, Object>> response = anonymousAuthController.getAnonymousStats();

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        Map<String, Object> responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(5L, responseBody.get("activeAnonymousUsers"));

        verify(anonymousUserService).getActiveAnonymousUsersCount();
    }

    @Test
    void recoverAnonymousAccess_WithUserAgent() {
        Map<String, Object> expectedResponse = new HashMap<>();
        expectedResponse.put("anonymousToken", "test-token");
        expectedResponse.put("expiresIn", 1800);
        expectedResponse.put("anonymousUserId", 1L);

        when(anonymousUserService.createAnonymousToken(
                anyString(),
                eq(TEST_USER_AGENT),
                eq(TEST_IP)
        )).thenReturn(expectedResponse);

        when(anonymousUserService.findByDeviceId(anyString())).thenReturn(Optional.empty());

        ResponseEntity<Map<String, Object>> response = anonymousAuthController.recoverAnonymousAccess(
                TEST_USER_AGENT,
                TEST_SCREEN_RESOLUTION,
                request
        );

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        Map<String, Object> responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(expectedResponse, responseBody);
        assertNotNull(responseBody.get("deviceId"));
        assertTrue(responseBody.get("deviceId").toString().startsWith("recovered-"));
        assertFalse((Boolean) responseBody.get("isRecovered"));

        verify(anonymousUserService).createAnonymousToken(anyString(), eq(TEST_USER_AGENT), eq(TEST_IP));
        verify(anonymousUserService).findByDeviceId(anyString());
    }

    @Test
    void recoverAnonymousAccess_ExistingUser() {
        Map<String, Object> expectedResponse = new HashMap<>();
        expectedResponse.put("anonymousToken", "test-token");
        expectedResponse.put("expiresIn", 1800);
        expectedResponse.put("anonymousUserId", 1L);

        AnonymousUser existingUser = AnonymousUser.builder()
                .anonymousUserId(1L)
                .deviceId("recovered-123")
                .build();

        when(anonymousUserService.createAnonymousToken(
                anyString(),
                eq(TEST_USER_AGENT),
                eq(TEST_IP)
        )).thenReturn(expectedResponse);

        when(anonymousUserService.findByDeviceId(anyString())).thenReturn(Optional.of(existingUser));

        ResponseEntity<Map<String, Object>> response = anonymousAuthController.recoverAnonymousAccess(
                TEST_USER_AGENT,
                TEST_SCREEN_RESOLUTION,
                request
        );

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        Map<String, Object> responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(expectedResponse, responseBody);
        assertNotNull(responseBody.get("deviceId"));
        assertTrue(responseBody.get("deviceId").toString().startsWith("recovered-"));
        assertTrue((Boolean) responseBody.get("isRecovered"));

        verify(anonymousUserService).createAnonymousToken(anyString(), eq(TEST_USER_AGENT), eq(TEST_IP));
        verify(anonymousUserService).findByDeviceId(anyString());
    }

    @Test
    void recoverAnonymousAccess_WithXForwardedFor() {
        request.addHeader("X-Forwarded-For", "10.0.0.1");
        Map<String, Object> expectedResponse = new HashMap<>();
        expectedResponse.put("anonymousToken", "test-token");
        expectedResponse.put("expiresIn", 1800);
        expectedResponse.put("anonymousUserId", 1L);

        when(anonymousUserService.createAnonymousToken(
                anyString(),
                eq(TEST_USER_AGENT),
                eq("10.0.0.1")
        )).thenReturn(expectedResponse);

        when(anonymousUserService.findByDeviceId(anyString())).thenReturn(Optional.empty());

        ResponseEntity<Map<String, Object>> response = anonymousAuthController.recoverAnonymousAccess(
                TEST_USER_AGENT,
                TEST_SCREEN_RESOLUTION,
                request
        );

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        Map<String, Object> responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(expectedResponse, responseBody);
        assertNotNull(responseBody.get("deviceId"));
        assertTrue(responseBody.get("deviceId").toString().startsWith("recovered-"));
        assertFalse((Boolean) responseBody.get("isRecovered"));

        verify(anonymousUserService).createAnonymousToken(anyString(), eq(TEST_USER_AGENT), eq("10.0.0.1"));
        verify(anonymousUserService).findByDeviceId(anyString());
    }
} 