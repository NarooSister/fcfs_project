package com.sparta.fcfsproject.auth.service;

import com.sparta.fcfsproject.auth.dto.CustomUserDetails;
import com.sparta.fcfsproject.auth.dto.LoginRequestDto;
import com.sparta.fcfsproject.auth.entity.User;
import com.sparta.fcfsproject.auth.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;


@Service
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public UserService(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }


    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        User userData = userRepository.findByEmail(email);

        if (userData == null) {
            // 이메일로 사용자를 찾지 못했을 경우 예외 발생
            throw new UsernameNotFoundException("User not found with email: " + email);
        }
        // 사용자 정보가 존재할 경우 UserDetails로 변환
        return new CustomUserDetails(userData);
    }
    public void signup(LoginRequestDto requestDto) {

        String email = requestDto.getEmail();
        String password = requestDto.getPassword();

        Boolean isExist = userRepository.existsByEmail(email);

        if (isExist) {
            throw new IllegalArgumentException("Email already exists.");
        }

        User data = new User();
        data.setEmail(email);
        data.setPassword(bCryptPasswordEncoder.encode(password));
        data.setRole("ROLE_ADMIN");
        userRepository.save(data);
    }
    public void login(LoginRequestDto requestDto) {
        String email = requestDto.getEmail();
        String password = requestDto.getPassword();

        User user = userRepository.findByEmail(email);

        if (user == null || !bCryptPasswordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password.");
        }

        // 로그인 성공 시 사용자 정보를 반환 (CustomUserDetails를 사용한 예시)
        new CustomUserDetails(user);
    }

}

