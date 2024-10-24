package com.sparta.fcfsproject.auth.config;

import com.sparta.fcfsproject.auth.dto.CustomUserDetails;
import com.sparta.fcfsproject.auth.entity.User;
import com.sparta.fcfsproject.auth.repository.UserRepository;
import com.sparta.fcfsproject.common.exception.UserBusinessException;
import com.sparta.fcfsproject.common.exception.UserServiceErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthFacade {
    private static UserRepository userRepository;
    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        AuthFacade.userRepository = userRepository;  // static 필드에 주입
    }

    // 현재 인증된 사용자의 Authentication 객체를 가져오는 메서드
    public static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    // 인증된 사용자(User)를 가져오는 메서드
    public static User getCurrentUser() {
        Authentication authentication = getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            String username = userDetails.getUsername();

            // DB에서 완전한 사용자 정보 조회
            return userRepository.findByUsername(username)
                    .orElseThrow(() -> new UserBusinessException(UserServiceErrorCode.AUTH_USER_NOT_FOUND));
        }
        return null; // 인증되지 않은 경우
    }

    // 인증된 사용자의 username을 가져오는 메서드
    public static String getCurrentUsername() {
        Authentication authentication = getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            return userDetails.getUsername();
        }
        return null;
    }
}
