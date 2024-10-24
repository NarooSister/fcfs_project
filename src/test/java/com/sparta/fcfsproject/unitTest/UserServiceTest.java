package com.sparta.fcfsproject.unitTest;

import com.sparta.fcfsproject.auth.dto.SignupRequest;
import com.sparta.fcfsproject.auth.dto.UpdatePasswordRequest;
import com.sparta.fcfsproject.auth.dto.UpdateProfileRequest;
import com.sparta.fcfsproject.auth.entity.User;
import com.sparta.fcfsproject.auth.repository.UserRepository;
import com.sparta.fcfsproject.auth.service.EncryptionService;
import com.sparta.fcfsproject.auth.service.UserService;
import com.sparta.fcfsproject.common.exception.UserBusinessException;
import com.sparta.fcfsproject.common.exception.UserServiceErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Mock
    private EncryptionService encryptionService;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Spy
    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }
    @Test
    @DisplayName("회원가입 시 개인정보 암호화 확인 테스트 ")
    void signup_ValidRequest_SavesEncryptedUserData() {
        // Given
        SignupRequest request = new SignupRequest("username", "email@example.com","password", "name", "010-1234-5678", "address");

        // 암호화 대상 필드에 대해 모킹 설정
        when(encryptionService.encrypt("email@example.com")).thenReturn("encryptedEmail");
        when(encryptionService.encrypt("name")).thenReturn("encryptedName");
        when(encryptionService.encrypt("010-1234-5678")).thenReturn("encryptedPhoneNumber");
        when(encryptionService.encrypt("address")).thenReturn("encryptedAddress");

        // 비밀번호에 대해 bcrypt 암호화를 모킹
        when(bCryptPasswordEncoder.encode("password")).thenReturn("encodedPassword");
        when(userRepository.existsByUsername(anyString())).thenReturn(false);

        // When
        userService.signup(request);

        // Then
        // 암호화된 정보가 저장되었는지 확인
        verify(userRepository, times(1)).save(any(User.class));

        // 각 필드에 대해 암호화가 올바르게 호출되었는지 확인
        verify(encryptionService).encrypt("email@example.com");
        verify(encryptionService).encrypt("name");
        verify(encryptionService).encrypt("010-1234-5678");
        verify(encryptionService).encrypt("address");

        // 비밀번호는 BCrypt로 인코딩 되었는지 확인
        verify(bCryptPasswordEncoder).encode("password");
    }
    @Test
    @DisplayName("중복된 사용자 이름일 경우 예외를 발생시키는지 테스트")
    void signup_DuplicateUsername_ThrowsUserBusinessException() {
        // Given
        SignupRequest request = new SignupRequest("duplicateUsername", "password", "email@example.com", "name", "010-1234-5678", "address");

        // 이미 존재하는 사용자 이름으로 설정
        when(userRepository.existsByUsername("duplicateUsername")).thenReturn(true);

        // When & Then
        // 중복된 사용자 이름일 경우 UserBusinessException 발생 여부를 검증
        UserBusinessException exception = assertThrows(UserBusinessException.class, () -> {
            userService.signup(request);
        });

        // 발생한 예외가 올바른 오류 코드(UserServiceErrorCode.DUPLICATE_USER)를 포함하는지 검증
        assertEquals(UserServiceErrorCode.DUPLICATE_USER, exception.getErrorCode());

        // userRepository.save()가 호출되지 않았는지 확인
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("사용자 정보 업데이트 - 유효한 요청 시 암호화된 사용자 정보를 업데이트")
    void updateProfile_ValidRequest_UpdatesEncryptedUserProfile() {
        // Given
        User user = new User("username", "encryptedEmail", "encodedPassword", "encryptedName", "encryptedPhoneNumber", "encryptedAddress", "ROLE_USER");
        UpdateProfileRequest request = new UpdateProfileRequest("010-1234-5678", "new address");

        when(encryptionService.encrypt("010-1234-5678")).thenReturn("encryptedPhoneNumber");
        when(encryptionService.encrypt("new address")).thenReturn("encryptedAddress");

        // When
        userService.updateProfile(user, request);

        // Then
        verify(encryptionService).encrypt("010-1234-5678");
        verify(encryptionService).encrypt("new address");
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("비밀번호 변경 - 유효한 요청 시 비밀번호를 업데이트하고 모든 세션에서 로그아웃")
    void updatePassword_ValidRequest_UpdatesPasswordAndLogsOutAllSessions() {
        // Given
        User user = new User("username", "email@example.com", "encodedPassword", "name", "010-1234-5678", "address", "ROLE_USER"); // 기존 비밀번호는 "encodedPassword"
        UpdatePasswordRequest request = new UpdatePasswordRequest("currentPassword", "newPassword", "newPassword");

        // 현재 비밀번호와 저장된 비밀번호가 일치한다고 설정
        when(bCryptPasswordEncoder.matches("currentPassword", "encodedPassword")).thenReturn(true);
        when(bCryptPasswordEncoder.encode("newPassword")).thenReturn("encodedNewPassword");

        // Redis 관련 동작 설정
        Set<String> sessionKeys = Set.of("username:session1", "username:session2");
        when(redisTemplate.keys("username:*")).thenReturn(sessionKeys);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(redisTemplate.getExpire(anyString(), eq(TimeUnit.MILLISECONDS))).thenReturn(1000L);

        // When
        userService.updatePassword(user, request);

        // Then
        // 현재 비밀번호가 저장된 비밀번호와 일치하는지 확인
        verify(bCryptPasswordEncoder).matches("currentPassword", "encodedPassword");
        // 새 비밀번호가 암호화되었는지 확인
        verify(bCryptPasswordEncoder).encode("newPassword");
        // 비밀번호 변경 후 저장했는지 확인
        verify(userRepository).save(user);
        // 모든 세션에서 로그아웃했는지 확인
        verify(userService).logoutAllSessions(user.getUsername());

        // Redis에서 세션 삭제 및 블랙리스트 추가 확인
        for (String key : sessionKeys) {
            verify(redisTemplate).getExpire(key, TimeUnit.MILLISECONDS);
            verify(redisTemplate.opsForValue()).set(eq(key), eq("true"), eq(1000L), eq(TimeUnit.MILLISECONDS));
            verify(redisTemplate).delete(key);
        }
    }
    @Test
    @DisplayName("비밀번호 변경 - 새 비밀번호와 확인 비밀번호가 일치하지 않을 경우 예외 발생")
    void updatePassword_NewPasswordMismatch_ThrowsUserBusinessException() {
        // Given
        User user = new User("username", "email@example.com", "encodedPassword", "name", "010-1234-5678", "address", "ROLE_USER");
        UpdatePasswordRequest request = new UpdatePasswordRequest("currentPassword", "newPassword", "mismatchPassword");

        // When & Then
        UserBusinessException exception = assertThrows(UserBusinessException.class, () -> {
            userService.updatePassword(user, request);
        });

        // 예외 메시지 검증 (필요한 경우)
        assertEquals(UserServiceErrorCode.PASSWORD_MISMATCH, exception.getErrorCode());
    }
    @Test
    @DisplayName("비밀번호 변경 - 현재 비밀번호가 틀릴 경우 예외 발생")
    void updatePassword_CurrentPasswordIncorrect_ThrowsUserBusinessException() {
        // Given
        User user = new User("username", "email@example.com", "encodedPassword", "name", "010-1234-5678", "address", "ROLE_USER");
        UpdatePasswordRequest request = new UpdatePasswordRequest("wrongCurrentPassword", "newPassword", "newPassword");

        // 현재 비밀번호가 일치하지 않는다고 설정
        when(bCryptPasswordEncoder.matches("wrongCurrentPassword", user.getPassword())).thenReturn(false);

        // When & Then
        UserBusinessException exception = assertThrows(UserBusinessException.class, () -> {
            userService.updatePassword(user, request);
        });

        // 예외 메시지 검증 (필요한 경우)
        assertEquals(UserServiceErrorCode.PASSWORD_INCORRECT, exception.getErrorCode());
    }
    @Test
    @DisplayName("모든 세션 로그아웃 - 세션이 없는 경우 오류 없이 종료")
    void logoutAllSessions_NoSessions_FinishesWithoutError() {
        // Given
        String username = "username";

        // Redis에서 세션이 없는 경우 설정
        when(redisTemplate.keys("username:*")).thenReturn(null);

        // When
        userService.logoutAllSessions(username);

        // Then
        verify(redisTemplate, never()).getExpire(anyString(), any(TimeUnit.class));
        verify(redisTemplate, never()).delete(anyString());
    }
}
