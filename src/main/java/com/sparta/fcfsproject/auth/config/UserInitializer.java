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
            String username = "kim123";
            String name = "김유저";
            String email = "kim123@gmail.com";
            String password = "1234";
            String role = "ROLE_USER";

            // 유저가 존재하지 않을 때만 추가
            if (userRepository.findByUsername(username) == null) {
                String encodedPassword = bCryptPasswordEncoder.encode(password);
                User tempUser = new User(1L, username, email, encodedPassword, name, "010-1234-1234", "경기도", role);
                userRepository.save(tempUser);
                System.out.println("Temporary user created: " + username);
            }
        };
    }
}
