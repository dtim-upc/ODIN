package edu.upc.essi.dtim.nextiabs.utils;

import java.util.Arrays;
import java.util.Objects;

public class Utils {
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
