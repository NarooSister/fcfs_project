package com.sparta.fcfsproject.auth.service;

import com.sparta.fcfsproject.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

@Service

public class EmailService {
    private final JavaMailSender mailSender;
    private final RedisTemplate<String, String> redisTemplate;
    private final EncryptionService encryptionService;
    private final UserRepository userRepository;

    public EmailService(JavaMailSender mailSender, RedisTemplate<String, String> redisTemplate, EncryptionService encryptionService, UserRepository userRepository) {
        this.mailSender = mailSender;
        this.redisTemplate = redisTemplate;
        this.encryptionService = encryptionService;
        this.userRepository = userRepository;
    }

    // 인증 번호 이메일로 전송
    public void sendVerificationEmail(String email) {
        // 이메일 존재 여부 확인
        existsByEmail(email);

        // 인증 번호 생성
        String verificationCode = generateVerificationCode();

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("이메일 인증 코드");
        message.setText("이메일 인증을 위해 아래 코드를 입력하세요: " + verificationCode);
        mailSender.send(message);

        // Redis에 인증 번호를 5분 동안 저장
        redisTemplate.opsForValue().set(email, verificationCode, 5, TimeUnit.MINUTES);
    }

    // 인증 번호 생성 (문자와 숫자를 포함한 난수 생성)
    public String generateVerificationCode() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder verificationCode = new StringBuilder();

        for (int i = 0; i < 8; i++) {  // 8자리 코드 생성
            int index = random.nextInt(characters.length());
            verificationCode.append(characters.charAt(index));
        }

        return verificationCode.toString();
    }

    // 이메일 확인
    public void existsByEmail(String email){
        // 이메일 암호화
        String encryptedEmail = encryptionService.encrypt(email);
        // 이메일이 이미 등록되어 있는지 확인
        if (userRepository.existsByEmail(encryptedEmail)) {
            throw new IllegalArgumentException("이미 등록된 이메일입니다.");
        }
    }

    // 인증 번호 확인
    public boolean verifyCode(String email, String inputCode) {
        String storedCode = redisTemplate.opsForValue().get(email);
        return inputCode.equals(storedCode);
    }
}
