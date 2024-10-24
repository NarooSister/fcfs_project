package com.sparta.fcfsproject.unitTest;

import com.sparta.fcfsproject.auth.repository.UserRepository;
import com.sparta.fcfsproject.auth.service.EmailService;
import com.sparta.fcfsproject.auth.service.EncryptionService;
import com.sparta.fcfsproject.common.exception.UserBusinessException;
import com.sparta.fcfsproject.common.exception.UserServiceErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class EmailServiceTest {
    @Mock
    private JavaMailSender mailSender;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private EncryptionService encryptionService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("이메일 전송 - 인증 코드 전송 및 Redis에 저장")
    void sendVerificationEmail_SendsEmailAndStoresCodeInRedis() {
        // Given
        String email = "test@example.com";

        // Redis에 저장될 ValueOperations Mock 설정
        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);

        when(encryptionService.encrypt(email)).thenReturn("encryptedEmail");
        when(userRepository.existsByEmail("encryptedEmail")).thenReturn(false);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // When
        emailService.sendVerificationEmail(email);

        // Then
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
        verify(valueOperations, times(1)).set(eq(email), anyString(), eq(5L), eq(TimeUnit.MINUTES));
    }


    @Test
    @DisplayName("이메일 전송 - 이메일이 이미 등록되어 있을 경우 예외 발생")
    void sendVerificationEmail_EmailAlreadyRegistered_ThrowsException() {
        // Given
        String email = "test@example.com";

        // 이메일 암호화 및 존재 확인 설정
        when(encryptionService.encrypt(email)).thenReturn("encryptedEmail");
        when(userRepository.existsByEmail("encryptedEmail")).thenReturn(true); // 이미 등록된 이메일로 설정

        // When & Then
        UserBusinessException exception = assertThrows(UserBusinessException.class, () -> {
            emailService.sendVerificationEmail(email);
        });

        assertEquals(UserServiceErrorCode.EMAIL_ALREADY_REGISTERED, exception.getErrorCode());
    }

    @Test
    @DisplayName("인증 코드 확인 - 올바른 인증 코드일 경우 true 반환")
    void verifyCode_ValidCode_ReturnsTrue() {
        // Given
        String email = "test@example.com";
        String inputCode = "ABCDEFGH";

        // Redis에 저장될 ValueOperations Mock 설정
        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(email)).thenReturn("ABCDEFGH");

        // When
        boolean result = emailService.verifyCode(email, inputCode);
        assertTrue(result);
    }


    @Test
    @DisplayName("인증 코드 확인 - 잘못된 인증 코드일 경우 예외 발생")
    void verifyCode_InvalidCode_ThrowsException() {
        // Given
        String email = "test@example.com";
        String inputCode = "INVALIDCODE";

        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(email)).thenReturn("ABCDEFGH");

        // When & Then
        UserBusinessException exception = assertThrows(UserBusinessException.class, () -> {
            emailService.verifyCode(email, inputCode);
        });
        assertEquals(UserServiceErrorCode.INVALID_VERIFICATION_CODE, exception.getErrorCode());
    }
}
