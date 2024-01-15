package edu.upc.essi.dtim.odin.utils;

import java.security.SecureRandom;

public class Utils {
    /**
     * Generates a universal unique identifier to be assigned to a dataset
     * @return A string containing the new UUID
     */
    public static String generateUUID() {
        final String characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder(16);
        SecureRandom random = new SecureRandom();
        sb.append("UUID_");
        for (int i = 0; i < 16; i++) {
            int randomIndex = random.nextInt(characters.length());
            sb.append(characters.charAt(randomIndex));
        }
        return sb.toString();
    }
}
