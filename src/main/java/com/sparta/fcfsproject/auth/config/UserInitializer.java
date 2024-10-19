package com.sparta.fcfsproject.auth.config;

import com.sparta.fcfsproject.auth.entity.User;
import com.sparta.fcfsproject.auth.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class UserInitializer {

    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public UserInitializer(BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    @Bean
    public CommandLineRunner createTemporaryUser(UserRepository userRepository) {
        return args -> {
            String username = "test";
            String name = "테스트 유저";
            String email = "test123@gmail.com";
            String password = "1234";
            String role = "ROLE_USER";

            // 유저가 존재하지 않을 때만 추가
            if (userRepository.findByUsername(username).isEmpty()) {
                String encodedPassword = bCryptPasswordEncoder.encode(password);
                User tempUser = new User(1L, username, email, encodedPassword, name, "010-1234-1234", "test", role);
                userRepository.save(tempUser);
                System.out.println("Temporary user created: " + username);
            }
        };
    }
}
