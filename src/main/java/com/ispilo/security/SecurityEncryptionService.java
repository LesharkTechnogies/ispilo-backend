package com.ispilo.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * Security Encryption Service using:
 * - RSA-4096: For asymmetric encryption (app private key ↔ server public key)
 * - AES-256: For symmetric encryption (message encryption)
 * - SHA-256: For hashing
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SecurityEncryptionService {

    private static final String RSA_ALGORITHM = "RSA/ECB/PKCS1Padding";
    private static final String RSA_KEY_ALGORITHM = "RSA";
    private static final int RSA_KEY_SIZE = 4096;

    private static final String AES_ALGORITHM = "AES";
    private static final int AES_KEY_SIZE = 256;

    private static final String HASH_ALGORITHM = "SHA-256";

    /**
     * Generate RSA key pair (4096-bit)
     * Server keeps private key, sends public key to app
     */
    public KeyPair generateRSAKeyPair() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance(RSA_KEY_ALGORITHM);
            keyGen.initialize(RSA_KEY_SIZE);
            KeyPair keyPair = keyGen.generateKeyPair();
            log.info("Generated RSA-4096 key pair");
            return keyPair;
        } catch (NoSuchAlgorithmException e) {
            log.error("Error generating RSA key pair", e);
            throw new RuntimeException("Failed to generate RSA key pair", e);
        }
    }

    /**
     * Generate AES-256 symmetric key
     * Used for encrypting messages/data
     */
    public SecretKey generateAESKey() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance(AES_ALGORITHM);
            keyGen.init(AES_KEY_SIZE);
            SecretKey secretKey = keyGen.generateKey();
            log.info("Generated AES-256 key");
            return secretKey;
        } catch (NoSuchAlgorithmException e) {
            log.error("Error generating AES key", e);
            throw new RuntimeException("Failed to generate AES key", e);
        }
    }

    /**
     * Encrypt data using public key (RSA)
     * Client uses this to encrypt with server's public key
     */
    public String encryptWithPublicKey(String data, PublicKey publicKey) {
        try {
            Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] encryptedData = cipher.doFinal(data.getBytes());
            String encrypted = Base64.getEncoder().encodeToString(encryptedData);
            log.debug("Data encrypted with public key (RSA-4096)");
            return encrypted;
        } catch (Exception e) {
            log.error("Error encrypting with public key", e);
            throw new RuntimeException("Failed to encrypt with public key", e);
        }
    }

    /**
     * Decrypt data using private key (RSA)
     * Server uses this to decrypt client's data
     */
    public String decryptWithPrivateKey(String encryptedData, PrivateKey privateKey) {
        try {
            Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] decodedData = Base64.getDecoder().decode(encryptedData);
            byte[] decryptedData = cipher.doFinal(decodedData);
            String decrypted = new String(decryptedData);
            log.debug("Data decrypted with private key (RSA-4096)");
            return decrypted;
        } catch (Exception e) {
            log.error("Error decrypting with private key", e);
            throw new RuntimeException("Failed to decrypt with private key", e);
        }
    }

    /**
     * Encrypt message using AES-256
     * Symmetric encryption for faster processing of large data
     */
    public String encryptWithAES(String data, String aesKeyString) {
        try {
            byte[] decodedKey = Base64.getDecoder().decode(aesKeyString);
            // Correct constructor for SecretKeySpec
            SecretKey originalKey = new SecretKeySpec(decodedKey, AES_ALGORITHM);

            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, originalKey);
            byte[] encryptedData = cipher.doFinal(data.getBytes());
            String encrypted = Base64.getEncoder().encodeToString(encryptedData);
            log.debug("Data encrypted with AES-256");
            return encrypted;
        } catch (Exception e) {
            log.error("Error encrypting with AES", e);
            throw new RuntimeException("Failed to encrypt with AES", e);
        }
    }

    /**
     * Decrypt message using AES-256
     */
    public String decryptWithAES(String encryptedData, String aesKeyString) {
        try {
            byte[] decodedKey = Base64.getDecoder().decode(aesKeyString);
            // Correct constructor for SecretKeySpec
            SecretKey originalKey = new SecretKeySpec(decodedKey, AES_ALGORITHM);

            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, originalKey);
            byte[] decodedData = Base64.getDecoder().decode(encryptedData);
            byte[] decryptedData = cipher.doFinal(decodedData);
            String decrypted = new String(decryptedData);
            log.debug("Data decrypted with AES-256");
            return decrypted;
        } catch (Exception e) {
            log.error("Error decrypting with AES", e);
            throw new RuntimeException("Failed to decrypt with AES", e);
        }
    }

    /**
     * Hash data using SHA-256
     * For integrity verification
     */
    public String hashWithSHA256(String data) {
        try {
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
            byte[] hashedData = digest.digest(data.getBytes());
            String hashed = Base64.getEncoder().encodeToString(hashedData);
            log.debug("Data hashed with SHA-256");
            return hashed;
        } catch (NoSuchAlgorithmException e) {
            log.error("Error hashing with SHA-256", e);
            throw new RuntimeException("Failed to hash with SHA-256", e);
        }
    }

    /**
     * Convert public key to Base64 string for transmission
     */
    public String publicKeyToString(PublicKey publicKey) {
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }

    /**
     * Convert Base64 string back to public key
     */
    public PublicKey stringToPublicKey(String publicKeyString) {
        try {
            byte[] decodedKey = Base64.getDecoder().decode(publicKeyString);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(decodedKey);
            KeyFactory keyFactory = KeyFactory.getInstance(RSA_KEY_ALGORITHM);
            return keyFactory.generatePublic(spec);
        } catch (Exception e) {
            log.error("Error converting string to public key", e);
            throw new RuntimeException("Failed to convert string to public key", e);
        }
    }

    /**
     * Convert AES key to Base64 string for storage/transmission
     */
    public String aesKeyToString(SecretKey key) {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    /**
     * Generate app private key (16-digit numeric string)
     * This is different from RSA private key - it's the app's secret key
     */
    public String generateAppPrivateKey() {
        StringBuilder key = new StringBuilder();
        SecureRandom random = new SecureRandom();
        for (int i = 0; i < 16; i++) {
            key.append(random.nextInt(10));
        }
        return key.toString();
    }

    /**
     * Generate app ID (UUID)
     */
    public String generateAppId() {
        return java.util.UUID.randomUUID().toString();
    }

    /**
     * Verify data integrity using SHA-256 hash
     */
    public boolean verifyDataIntegrity(String data, String hash) {
        String calculatedHash = hashWithSHA256(data);
        return calculatedHash.equals(hash);
    }
}
