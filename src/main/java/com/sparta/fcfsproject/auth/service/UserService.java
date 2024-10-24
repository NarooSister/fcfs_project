package com.sparta.fcfsproject.auth.service;

import com.sparta.fcfsproject.auth.dto.CustomUserDetails;
import com.sparta.fcfsproject.auth.dto.SignupRequest;
import com.sparta.fcfsproject.auth.dto.UpdatePasswordRequest;
import com.sparta.fcfsproject.auth.dto.UpdateProfileRequest;
import com.sparta.fcfsproject.auth.entity.User;
import com.sparta.fcfsproject.auth.repository.UserRepository;
import com.sparta.fcfsproject.common.exception.UserBusinessException;
import com.sparta.fcfsproject.common.exception.UserServiceErrorCode;
import jakarta.transaction.Transactional;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final RedisTemplate<String, Object> redisTemplate;
    private final EncryptionService encryptionService;

    public UserService(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder, RedisTemplate<String, Object> redisTemplate, EncryptionService encryptionService) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.redisTemplate = redisTemplate;
        this.encryptionService = encryptionService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User userData = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
        // 사용자 정보가 존재할 경우 UserDetails로 변환
        return new CustomUserDetails(userData);
    }

    @Transactional
    public void signup(SignupRequest request) {
        // 회원정보를 암호화
        // 이메일, 이름, 전화번호, 주소 암호화
        String encryptedEmail = encryptionService.encrypt(request.getEmail());
        String encryptedName = encryptionService.encrypt(request.getName());
        String encryptedPhoneNumber = encryptionService.encrypt(request.getPhoneNumber());
        String encryptedAddress = encryptionService.encrypt(request.getAddress());

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserBusinessException(UserServiceErrorCode.DUPLICATE_USER);
        }

        // 비밀번호 암호화
        String encodedPassword = bCryptPasswordEncoder.encode(request.getPassword());

        // 사용자 엔티티 생성 및 저장
        User user = new User(request.getUsername(), encryptedEmail, encodedPassword, encryptedName,
                encryptedPhoneNumber, encryptedAddress, "ROLE_USER");
        userRepository.save(user);
    }

    @Transactional
    public void updateProfile(User user, UpdateProfileRequest request) {
        String encryptedPhoneNumber = encryptionService.encrypt(request.getPhoneNumber());
        String encryptedAddress = encryptionService.encrypt(request.getAddress());
        user.updateProfile(encryptedPhoneNumber, encryptedAddress);
        // 사용자 정보 저장
        userRepository.save(user);
    }

    @Transactional
    public void updatePassword(User user, UpdatePasswordRequest request) {
        // 새 비밀번호와 확인 비밀번호가 일치하는지 확인
        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            throw new UserBusinessException(UserServiceErrorCode.PASSWORD_MISMATCH);
        }

        // 기존 비밀번호가 일치하는지 확인
        if (!bCryptPasswordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new UserBusinessException(UserServiceErrorCode.PASSWORD_INCORRECT);
        }

        // 새 비밀번호로 변경
        String encodedNewPassword = bCryptPasswordEncoder.encode(request.getNewPassword());
        user.updatePassword(encodedNewPassword);

        // 사용자 정보 저장
        userRepository.save(user);

        // 모든 기기에서 로그아웃
        logoutAllSessions(user.getUsername());
    }

    // 모든 세션에서 로그아웃하고 블랙리스트에 추가
    public void logoutAllSessions(String username) {
        // Redis에서 해당 이메일로 저장된 모든 키를 조회
        Set<String> keys = redisTemplate.keys(username + ":*");  // username으로 시작하는 모든 키 가져오기 (세션별로 저장됨)

        if (keys != null && !keys.isEmpty()) {
            // 모든 refresh 토큰 삭제
            for (String key : keys) {
                // 기존 refresh 토큰의 TTL 조회
                Long existingTtl = redisTemplate.getExpire(key, TimeUnit.MILLISECONDS);

                if (existingTtl != null && existingTtl > 0) {
                    // 블랙리스트에 추가 (기존 토큰의 TTL과 동일하게 유지)
                    redisTemplate.opsForValue().set(key, "true", existingTtl, TimeUnit.MILLISECONDS);
                }
                redisTemplate.delete(key);
            }
        }
    }
}

