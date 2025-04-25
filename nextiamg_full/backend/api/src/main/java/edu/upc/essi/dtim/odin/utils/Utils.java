package edu.upc.essi.dtim.odin.utils;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Objects;

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

    public static String reformatName(String tableName) {
        // Trim whitespaces at the end and the beginning
        tableName = tableName.trim();
        // Remove characters that might case problems
        String pattern = "[!@#$%^&*)-+=\\[\\]{}\\\\|;:'\"<>,.?/]";
        tableName = tableName.replaceAll(pattern, "");
        // Pass the string to camel case
        tableName = toCamelCase(tableName);

        return tableName;
    }

    public static String toCamelCase(String inputString) {
        StringBuilder camelCaseString = new StringBuilder();
        String[] words = inputString.split("\\s+|(?<=\\()|(?=\\))"); // Split the input string by whitespace or parenthesis
        words = Arrays.stream(words).filter(str -> !Objects.equals(str, "(")).toArray(String[]::new);
        words = Arrays.stream(words).map(str -> str.replace("(", "")).toArray(String[]::new);

        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            if (i == 0) {
                camelCaseString.append(word.toLowerCase()); // Convert the first word to lowercase
            } else {
                camelCaseString.append(Character.toUpperCase(word.charAt(0))); // Capitalize the first letter
                camelCaseString.append(word.substring(1).toLowerCase()); // Convert the rest to lowercase
            }
        }

        return camelCaseString.toString();
    }
}
