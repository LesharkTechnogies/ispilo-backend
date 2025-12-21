package com.ispilo.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * AES-256-GCM encryption service for end-to-end message encryption
 */
@Service
@Slf4j
public class EncryptionService {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12; // 96 bits
    private static final int GCM_TAG_LENGTH = 128; // 128 bits
    private static final int AES_KEY_SIZE = 256;

    @Value("${app.encryption.master-key:}")
    private String masterKeyBase64;

    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Generate a new AES-256 key for conversation encryption
     */
    public String generateConversationKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
            keyGenerator.init(AES_KEY_SIZE);
            SecretKey secretKey = keyGenerator.generateKey();
            return Base64.getEncoder().encodeToString(secretKey.getEncoded());
        } catch (Exception e) {
            log.error("Error generating conversation key", e);
            throw new RuntimeException("Failed to generate encryption key", e);
        }
    }

    /**
     * Encrypt message content using AES-256-GCM
     * @param plainText The message to encrypt
     * @param conversationKeyBase64 The conversation-specific encryption key
     * @return Base64 encoded encrypted data (IV + ciphertext + tag)
     */
    public String encrypt(String plainText, String conversationKeyBase64) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(conversationKeyBase64);
            SecretKey secretKey = new SecretKeySpec(keyBytes, ALGORITHM);

            // Generate random IV
            byte[] iv = new byte[GCM_IV_LENGTH];
            secureRandom.nextBytes(iv);

            // Initialize cipher
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);

            // Encrypt
            byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            // Combine IV + ciphertext
            ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + cipherText.length);
            byteBuffer.put(iv);
            byteBuffer.put(cipherText);

            // Return as Base64
            return Base64.getEncoder().encodeToString(byteBuffer.array());

        } catch (Exception e) {
            log.error("Error encrypting message", e);
            throw new RuntimeException("Encryption failed", e);
        }
    }

    /**
     * Decrypt message content using AES-256-GCM
     * @param encryptedDataBase64 Base64 encoded encrypted data (IV + ciphertext + tag)
     * @param conversationKeyBase64 The conversation-specific encryption key
     * @return Decrypted plaintext message
     */
    public String decrypt(String encryptedDataBase64, String conversationKeyBase64) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(conversationKeyBase64);
            SecretKey secretKey = new SecretKeySpec(keyBytes, ALGORITHM);

            // Decode the encrypted data
            byte[] encryptedData = Base64.getDecoder().decode(encryptedDataBase64);

            // Extract IV and ciphertext
            ByteBuffer byteBuffer = ByteBuffer.wrap(encryptedData);
            byte[] iv = new byte[GCM_IV_LENGTH];
            byteBuffer.get(iv);
            byte[] cipherText = new byte[byteBuffer.remaining()];
            byteBuffer.get(cipherText);

            // Initialize cipher for decryption
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);

            // Decrypt
            byte[] plainText = cipher.doFinal(cipherText);

            return new String(plainText, StandardCharsets.UTF_8);

        } catch (Exception e) {
            log.error("Error decrypting message", e);
            throw new RuntimeException("Decryption failed", e);
        }
    }

    /**
     * Encrypt conversation key with user's public key (for key exchange)
     * This is a simplified version - in production, use RSA or ECDH for key exchange
     */
    public String encryptKeyForUser(String conversationKey, String userPublicKey) {
        // For now, return the key as-is
        // In production, implement proper public key encryption (RSA-OAEP or similar)
        return conversationKey;
    }

    /**
     * Decrypt conversation key with user's private key
     */
    public String decryptKeyForUser(String encryptedKey, String userPrivateKey) {
        // For now, return the key as-is
        // In production, implement proper private key decryption
        return encryptedKey;
    }
}

