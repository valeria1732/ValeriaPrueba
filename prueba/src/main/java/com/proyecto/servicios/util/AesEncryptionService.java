package com.proyecto.servicios.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

@Component
public class AesEncryptionService {

    private static final int IV_LENGTH = 16;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final byte[] EPHEMERAL_KEY = generarBytesAleatorios(32);

    private final String secretKey;
    private final String algorithm;

    public AesEncryptionService(
            @Value("${gestopago.security.aes-key:}") String secretKey,
            @Value("${gestopago.security.aes-algorithm:AES/CBC/PKCS5Padding}") String algorithm) {
        this.secretKey = secretKey;
        this.algorithm = StringUtils.defaultIfBlank(algorithm, "AES/CBC/PKCS5Padding");
    }

    public String cifrar(String plainText) {
        if (plainText == null) {
            return null;
        }
        try {
            SecretKeySpec keySpec = new SecretKeySpec(resolverKeyBytes(), "AES");
            Cipher cipher = Cipher.getInstance(algorithm);
            byte[] inputBytes = plainText.getBytes(StandardCharsets.UTF_8);

            if (esModoCbc()) {
                byte[] iv = new byte[IV_LENGTH];
                SECURE_RANDOM.nextBytes(iv);
                cipher.init(Cipher.ENCRYPT_MODE, keySpec, new IvParameterSpec(iv));
                byte[] cipherBytes = cipher.doFinal(inputBytes);
                byte[] combined = new byte[IV_LENGTH + cipherBytes.length];
                System.arraycopy(iv, 0, combined, 0, IV_LENGTH);
                System.arraycopy(cipherBytes, 0, combined, IV_LENGTH, cipherBytes.length);
                return Base64.getEncoder().encodeToString(combined);
            }

            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            return Base64.getEncoder().encodeToString(cipher.doFinal(inputBytes));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Error al cifrar datos con AES", e);
        }
    }

    public String descifrar(String base64CipherText) {
        if (base64CipherText == null) {
            return null;
        }
        try {
            byte[] decoded = Base64.getDecoder().decode(base64CipherText);
            SecretKeySpec keySpec = new SecretKeySpec(resolverKeyBytes(), "AES");
            Cipher cipher = Cipher.getInstance(algorithm);

            if (esModoCbc()) {
                if (decoded.length <= IV_LENGTH) {
                    throw new IllegalArgumentException("Payload cifrado inválido o incompleto");
                }
                byte[] iv = Arrays.copyOfRange(decoded, 0, IV_LENGTH);
                byte[] cipherBytes = Arrays.copyOfRange(decoded, IV_LENGTH, decoded.length);
                cipher.init(Cipher.DECRYPT_MODE, keySpec, new IvParameterSpec(iv));
                return new String(cipher.doFinal(cipherBytes), StandardCharsets.UTF_8);
            }

            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            return new String(cipher.doFinal(decoded), StandardCharsets.UTF_8);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Error al descifrar datos con AES", e);
        }
    }

    private boolean esModoCbc() {
        return algorithm.toUpperCase().contains("/CBC/");
    }

    private byte[] resolverKeyBytes() throws NoSuchAlgorithmException {
        if (StringUtils.isBlank(secretKey)) {
            return EPHEMERAL_KEY;
        }
        byte[] raw = secretKey.getBytes(StandardCharsets.UTF_8);
        return (raw.length == 16 || raw.length == 24 || raw.length == 32)
                ? raw
                : MessageDigest.getInstance("SHA-256").digest(raw);
    }

    private static byte[] generarBytesAleatorios(int length) {
        byte[] bytes = new byte[length];
        SECURE_RANDOM.nextBytes(bytes);
        return bytes;
    }
}
