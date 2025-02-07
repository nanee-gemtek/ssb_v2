package com.mysite.sbb;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.security.MessageDigest;

//SHA-1 기반 PasswordEncoder 구현
public class Sha1PasswordEncoder implements PasswordEncoder {
    @Override
    public String encode(CharSequence rawPassword) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] digest = md.digest(rawPassword.toString().getBytes("UTF-8"));
            return convertToHex(digest);
        } catch (Exception e) {
            throw new RuntimeException("SHA-1 encoding error", e);
        }
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        return encode(rawPassword).equals(encodedPassword);
    }

    private String convertToHex(byte[] data) {
        StringBuilder buf = new StringBuilder();
        for (byte b : data) {
            int halfbyte = (b >>> 4) & 0x0F;
            int twoHalfs = 0;
            do {
                buf.append((char) ((0 <= halfbyte && halfbyte <= 9) ? ('0' + halfbyte) : ('a' + (halfbyte - 10))));
                halfbyte = b & 0x0F;
            } while (twoHalfs++ < 1);
        }
        return buf.toString();
    }
}
