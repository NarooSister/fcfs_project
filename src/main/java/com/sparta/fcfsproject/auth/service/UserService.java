package com.sparta.fcfsproject.auth.service;

import com.sparta.fcfsproject.auth.dto.CustomUserDetails;
import com.sparta.fcfsproject.auth.dto.SignupRequest;
import com.sparta.fcfsproject.auth.entity.User;
import com.sparta.fcfsproject.auth.repository.UserRepository;
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

    public UserService(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder, RedisTemplate<String, Object> redisTemplate) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User userData = userRepository.findByUsername(username);

        if (userData == null) {
            // 이메일로 사용자를 찾지 못했을 경우 예외 발생
            throw new UsernameNotFoundException("User not found with username: " + username);
        }
        // 사용자 정보가 존재할 경우 UserDetails로 변환
        return new CustomUserDetails(userData);
    }

    public void signup(SignupRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("이미 사용 중인 아이디 입니다.");
        }
        // 비밀번호 암호화
        String encodedPassword = bCryptPasswordEncoder.encode(request.getPassword());

        // 사용자 엔티티 생성 및 저장
        User user = new User(request.getUsername(), request.getEmail(), encodedPassword, request.getName(),
                request.getPhoneNumber(), request.getAddress(), "ROLE_USER");
        userRepository.save(user);
    }

    // 모든 세션에서 로그아웃하는 메서드
    public void logoutAllSessions(String username) {
        // Redis에서 해당 이메일로 저장된 모든 키를 조회
        Set<String> keys = redisTemplate.keys(username + ":*");  // username으로 시작하는 모든 키 가져오기 (세션별로 저장됨)

        if (keys != null && !keys.isEmpty()) {
            // 모든 refresh 토큰 삭제
            for (String key : keys) {
                redisTemplate.delete(key);
            }
        }
    }

    // 블랙리스트 처리 (선택 사항)
    public void blacklistTokens(Set<String> tokens, Long ttl) {
        for (String token : tokens) {
            redisTemplate.opsForValue().set(token, "true", ttl, TimeUnit.MILLISECONDS);
        }
    }

}

