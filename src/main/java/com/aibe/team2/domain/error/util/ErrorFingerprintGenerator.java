package com.aibe.team2.domain.error.util;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Component
public class ErrorFingerprintGenerator {

    public String normalizeMessage(String message) {
        if (message == null || message.isBlank()) {
            return "NO_MESSAGE";
        }

        return message
                .replaceAll("\\b[0-9]+\\b", "<NUM>")
                .replaceAll("\\b[0-9a-fA-F\\-]{8,}\\b", "<ID>")
                .replaceAll("/[A-Za-z0-9_\\-/.]+", "<PATH>")
                .trim();
    }

    public String generate(String errorCode, String exceptionType, String message) {
        String normalized = normalizeMessage(message);
        String source = safe(errorCode) + "|" + safe(exceptionType) + "|" + normalized;
        return sha256(source);
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "UNKNOWN" : value;
    }

    private String sha256(String source) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(source.getBytes(StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not found", e);
        }
    }
}