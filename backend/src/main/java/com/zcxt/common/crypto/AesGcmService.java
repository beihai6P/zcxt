package com.zcxt.common.crypto;

import com.zcxt.config.AppProperties;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class AesGcmService {
    private static final int IV_LEN = 12;
    private static final int TAG_LEN_BIT = 128;

    private final AppProperties appProperties;
    private final SecureRandom secureRandom = new SecureRandom();

    public AesGcmService(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    public String encryptToBase64(String plaintext) {
        try {
            byte[] iv = new byte[IV_LEN];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key(), new GCMParameterSpec(TAG_LEN_BIT, iv));
            byte[] cipherText = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            byte[] out = new byte[iv.length + cipherText.length];
            System.arraycopy(iv, 0, out, 0, iv.length);
            System.arraycopy(cipherText, 0, out, iv.length, cipherText.length);
            return Base64.getUrlEncoder().withoutPadding().encodeToString(out);
        } catch (Exception e) {
            throw new IllegalStateException("加密失败");
        }
    }

    public String decryptFromBase64(String base64) {
        try {
            byte[] all = Base64.getUrlDecoder().decode(base64);
            byte[] iv = new byte[IV_LEN];
            byte[] cipherText = new byte[all.length - IV_LEN];
            System.arraycopy(all, 0, iv, 0, IV_LEN);
            System.arraycopy(all, IV_LEN, cipherText, 0, cipherText.length);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, key(), new GCMParameterSpec(TAG_LEN_BIT, iv));
            byte[] plain = cipher.doFinal(cipherText);
            return new String(plain, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("解密失败");
        }
    }

    private SecretKey key() {
        try {
            var raw = appProperties.getCrypto().getAesKey().getBytes(StandardCharsets.UTF_8);
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(raw);
            return new SecretKeySpec(digest, "AES");
        } catch (Exception e) {
            throw new IllegalStateException("密钥初始化失败");
        }
    }
}

