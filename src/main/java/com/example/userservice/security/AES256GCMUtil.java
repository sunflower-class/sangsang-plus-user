package com.example.userservice.security;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Hex;

public class AES256GCMUtil {
    private static final String ENCRYPT_ALGO = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128; // bits
    private static final int IV_LENGTH = 12; // bytes
    
    private final SecretKeySpec keySpec;
    private final SecureRandom secureRandom = new SecureRandom();

    public AES256GCMUtil(byte[] keyBytes) {
        this.keySpec = new SecretKeySpec(keyBytes, "AES");
    }

    public String encrypt(String plainText) throws Exception { 
        byte[] iv = new byte[IV_LENGTH];
        secureRandom.nextBytes(iv); // iv는 매번 다른 것을 사용한다.

        Cipher cipher = Cipher.getInstance(ENCRYPT_ALGO);
        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Ciper.ENCRYPT_MODE, keySpec, gcmSpec);

        byte[] encrypted = cipher.doFinal(plainText.getBytes("UTF-8"));

        // IV + 암호문을 합쳐서 Base64로 변환
        byte[] encryptedIvAndText = new byte[IV_LENGTH + encrypted.length];
        System.arraycopy(iv, 0, encryptedIvAndText, 0, IV_LENGTH);
        System.arraycopy(encrypted, 0, encryptedIvAndText, IV_LENGTH, encrypted.length);

        return Base64.getEncoder().encodeToString(encryptedIvAndText);
    }

    public String decrypt(String cipherText) throws Exception { 
        byte[] encryptedIvTextBytes = Base64.getEncoder().decode(cipherText);

        byte[] iv = new byte[IV_LENGTH];
        byte[] encryptedBytes = new byte[encryptedIvTextBytes.length - IV_LENGTH];
        System.arraycopy(encryptedIvTextBytes, 0, iv, 0, IV_LENGTH);
        System.arraycopy(encryptedIvTextBytes, IV_LENGTH, encryptedBytes, 0, encryptedBytes.length);

        Cipher cipher = Cipher.getInstance(ENCRYPT_ALGO);
        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec);

        byte[] decrypted = cipher.doFinal(encryptedBytes);
        return new String(decrypted, "UTF-8");
    }
}
