package com.example.userservice.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.example.userservice.security.AES256GCMUtil;

@Component
public class AES256GCMUtilProvider {
    private final AES256GCMUtil aesUtil;

    public AES256GCMUtilProvider(@Value("${encryption.key}") String hexKey){
        byte[] keyBytes = hexStringToByteArray(hexKey);
        this.aesUtil = new AES256GCMUtil(keyBytes);
    }

    public AES256GCMUtil get() {
        return aesUtil;
    }

    private byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for(int i=0; i < len; i += 2){
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }
}