package com.proyecto.servicios.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;

/**
 * Servicio criptográfico para firma y cifrado AES de payloads
 * según el flujo de integración con el proveedor GestoPago.
 */
@Slf4j
@Component
public class AesEncryptionService {

    private final String secretKey;
    private final String algorithm;
    private final String ivString;

    public AesEncryptionService(
            @Value("${gestopago.security.aes-key:GestoPagoAESSecretKey2026Secure32}") String secretKey,
            @Value("${gestopago.security.aes-algorithm:AES/CBC/PKCS5Padding}") String algorithm,
            @Value("${gestopago.security.aes-iv:1234567890123456}") String ivString) {
        this.secretKey = secretKey;
        this.algorithm = algorithm;
        this.ivString = ivString;
    }

    /**
     * Cifra un texto o payload JSON con AES y lo codifica en Base64.
     */
    public String cifrar(String plainText) {
        if (plainText == null) return null;
        try {
            byte[] keyBytes = getValidKeyBytes(secretKey);
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
            Cipher cipher = Cipher.getInstance(algorithm);

            if (algorithm.toUpperCase().contains("/CBC/")) {
                byte[] ivBytes = Arrays.copyOf(ivString.getBytes(StandardCharsets.UTF_8), 16);
                IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);
                cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
            } else {
                cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            }

            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            log.error("Error al cifrar payload con AES: {}", e.getMessage(), e);
            throw new RuntimeException("Error en proceso de cifrado AES: " + e.getMessage(), e);
        }
    }

    /**
     * Descifra un texto en Base64 cifrado con AES.
     */
    public String descifrar(String base64CipherText) {
        if (base64CipherText == null) return null;
        try {
            byte[] keyBytes = getValidKeyBytes(secretKey);
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
            Cipher cipher = Cipher.getInstance(algorithm);

            if (algorithm.toUpperCase().contains("/CBC/")) {
                byte[] ivBytes = Arrays.copyOf(ivString.getBytes(StandardCharsets.UTF_8), 16);
                IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);
                cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
            } else {
                cipher.init(Cipher.DECRYPT_MODE, keySpec);
            }

            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(base64CipherText));
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("Error al descifrar payload con AES: {}", e.getMessage(), e);
            throw new RuntimeException("Error en proceso de descifrado AES: " + e.getMessage(), e);
        }
    }

    private byte[] getValidKeyBytes(String key) throws Exception {
        byte[] bytes = key.getBytes(StandardCharsets.UTF_8);
        if (bytes.length == 16 || bytes.length == 24 || bytes.length == 32) {
            return bytes;
        }
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        return sha.digest(bytes);
    }
}
