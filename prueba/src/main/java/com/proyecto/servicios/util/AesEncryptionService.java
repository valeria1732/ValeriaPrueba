package com.proyecto.servicios.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

@Component
public class AesEncryptionService {

    private static final byte[] EPHEMERAL_KEY = generarBytesAleatorios(32);
    private static final byte[] EPHEMERAL_IV = generarBytesAleatorios(16);

    private final String secretKey;
    private final String algorithm;
    private final String ivString;

    public AesEncryptionService(
            @Value("${gestopago.security.aes-key:}") String secretKey,
            @Value("${gestopago.security.aes-algorithm:AES/CBC/PKCS5Padding}") String algorithm,
            @Value("${gestopago.security.aes-iv:}") String ivString) {
        this.secretKey = secretKey;
        this.algorithm = algorithm;
        this.ivString = ivString;
    }

    public String cifrar(String plainText) {
        if (plainText == null) return null;
        return Base64.getEncoder().encodeToString(procesar(Cipher.ENCRYPT_MODE, plainText.getBytes(StandardCharsets.UTF_8)));
    }

    public String descifrar(String base64CipherText) {
        if (base64CipherText == null) return null;
        return new String(procesar(Cipher.DECRYPT_MODE, Base64.getDecoder().decode(base64CipherText)), StandardCharsets.UTF_8);
    }

    private byte[] procesar(int mode, byte[] input) {
        try {
            byte[] keyBytes = resolverKeyBytes();
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
            Cipher cipher = Cipher.getInstance(algorithm);
            if (algorithm.toUpperCase().contains("/CBC/")) {
                byte[] iv = StringUtils.isNotBlank(ivString)
                        ? Arrays.copyOf(ivString.getBytes(StandardCharsets.UTF_8), 16)
                        : EPHEMERAL_IV;
                cipher.init(mode, keySpec, new IvParameterSpec(iv));
            } else {
                cipher.init(mode, keySpec);
            }
            return cipher.doFinal(input);
        } catch (Exception e) {
            throw new IllegalStateException("Error en operación criptográfica AES", e);
        }
    }

    private byte[] resolverKeyBytes() throws Exception {
        if (StringUtils.isBlank(secretKey)) {
            return EPHEMERAL_KEY;
        }
        byte[] raw = secretKey.getBytes(StandardCharsets.UTF_8);
        return (raw.length == 16 || raw.length == 24 || raw.length == 32)
                ? raw : MessageDigest.getInstance("SHA-256").digest(raw);
    }

    private static byte[] generarBytesAleatorios(int length) {
        byte[] bytes = new byte[length];
        new SecureRandom().nextBytes(bytes);
        return bytes;
    }
}
