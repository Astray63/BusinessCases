package com.eb.electricitybusiness.service;

import com.eb.electricitybusiness.service.impl.EmailServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class EmailServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private EmailServiceImpl emailService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        ReflectionTestUtils.setField(emailService, "brevoApiKey", "test-key");
        ReflectionTestUtils.setField(emailService, "senderEmail", "sender@test.com");
        ReflectionTestUtils.setField(emailService, "senderName", "Sender Name");

        // Overwrite the real RestTemplate created in constructor
        ReflectionTestUtils.setField(emailService, "restTemplate", restTemplate);
    }

    @Test
    void sendVerificationEmail_Success() {
        String to = "user@test.com";
        String name = "User";
        String code = "123456";

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class))).thenReturn(new ResponseEntity<>("OK", HttpStatus.OK));

        assertDoesNotThrow(() -> emailService.sendVerificationEmail(to, name, code));

        verify(restTemplate).exchange(
                eq("https://api.brevo.com/v3/smtp/email"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class));
    }

    @Test
    void sendVerificationEmail_ApiError_ThrowsException() {
        String to = "user@test.com";
        String name = "User";
        String code = "123456";

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class))).thenReturn(new ResponseEntity<>("Error", HttpStatus.BAD_REQUEST));

        assertThrows(RuntimeException.class, () -> emailService.sendVerificationEmail(to, name, code));
    }

    @Test
    void sendVerificationEmail_Exception_ThrowsRuntimeException() {
        String to = "user@test.com";
        String name = "User";
        String code = "123456";

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class))).thenThrow(new RuntimeException("Network error"));

        assertThrows(RuntimeException.class, () -> emailService.sendVerificationEmail(to, name, code));
    }
}
