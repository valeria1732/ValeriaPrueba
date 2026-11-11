package com.proyecto.servicios.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AesEncryptionServiceTest {

    @Test
    @DisplayName("Cifrado y descifrado con clave de 32 bytes y AES/CBC/PKCS5Padding debe usar IV aleatorio distinto por operación")
    void testCifrarYDescifrar_ConIvAleatorioDinamico() {
        AesEncryptionService service = new AesEncryptionService(
                "GestoPagoAESSecretKey2026Secure32",
                "AES/CBC/PKCS5Padding"
        );

        String original = "ReferenciaPago123456";
        String cifrado1 = service.cifrar(original);
        String cifrado2 = service.cifrar(original);

        assertNotNull(cifrado1);
        assertNotNull(cifrado2);
        assertNotEquals(original, cifrado1);
        assertNotEquals(cifrado1, cifrado2, "Cada cifrado debe generar un IV aleatorio diferente");

        assertEquals(original, service.descifrar(cifrado1));
        assertEquals(original, service.descifrar(cifrado2));
    }

    @Test
    @DisplayName("Debe derivar clave SHA-256 cuando el secreto no mide 16, 24 o 32 bytes y funcionar en modo ECB/AES sin IV")
    void testCifrarYDescifrar_ClaveCortaYModoSinIv() {
        AesEncryptionService service = new AesEncryptionService(
                "clave-corta",
                "AES/ECB/PKCS5Padding"
        );

        String original = "MensajeSeguro2026";
        String cifrado = service.cifrar(original);

        assertNotNull(cifrado);
        assertEquals(original, service.descifrar(cifrado));
    }

    @Test
    @DisplayName("Debe rechazar payload inválido al descifrar o algoritmo inexistente al cifrar")
    void testErroresCifradoYDescifrado() {
        AesEncryptionService cbcService = new AesEncryptionService(
                "GestoPagoAESSecretKey2026Secure32",
                "AES/CBC/PKCS5Padding"
        );
        assertThrows(IllegalArgumentException.class, () -> cbcService.descifrar("no-es-valido-base64-iv"));
        assertThrows(IllegalArgumentException.class, () -> cbcService.descifrar("AAAA"));

        AesEncryptionService invalidAlg = new AesEncryptionService(
                "GestoPagoAESSecretKey2026Secure32",
                "ALGORITMO_INEXISTENTE"
        );
        assertThrows(IllegalStateException.class, () -> invalidAlg.cifrar("texto"));
        String valido = cbcService.cifrar("texto");
        assertThrows(IllegalStateException.class, () -> invalidAlg.descifrar(valido));
    }

    @Test
    @DisplayName("Debe retornar null ante entradas nulas y usar clave efímera si secretKey viene vacío")
    void testEntradasNulasYClaveEfimera() {
        AesEncryptionService ephemeralService = new AesEncryptionService("", "AES/CBC/PKCS5Padding");
        assertNull(ephemeralService.cifrar(null));
        assertNull(ephemeralService.descifrar(null));

        String cifrado = ephemeralService.cifrar("dato-sensible");
        assertNotNull(cifrado);
        assertEquals("dato-sensible", ephemeralService.descifrar(cifrado));
    }
}

