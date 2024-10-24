package com.sparta.fcfsproject.auth.service;

import org.springframework.security.crypto.encrypt.BytesEncryptor;
import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;

@Service
public class EncryptionService {

    private final BytesEncryptor aesBytesEncryptor;

    public EncryptionService(BytesEncryptor aesBytesEncryptor) {
        this.aesBytesEncryptor = aesBytesEncryptor;
    }

    // 암호화
    public String encrypt(String data) {
        byte[] encrypted = aesBytesEncryptor.encrypt(data.getBytes(StandardCharsets.UTF_8));
        return new String(encrypted, StandardCharsets.UTF_8);
    }

    // 복호화
    public String decrypt(String encryptedData) {
        byte[] decrypted = aesBytesEncryptor.decrypt(encryptedData.getBytes(StandardCharsets.UTF_8));
        return new String(decrypted, StandardCharsets.UTF_8);
    }
}
