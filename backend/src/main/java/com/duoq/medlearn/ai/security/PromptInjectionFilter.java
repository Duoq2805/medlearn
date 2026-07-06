package com.duoq.medlearn.ai.security;

import com.duoq.medlearn.ai.exception.AiInjectionDetectedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@Service
@Slf4j
public class PromptInjectionFilter {

    private static final Pattern[] INJECTION_PATTERNS = {
            Pattern.compile("(?i)(?s)ignore\\s+(all\\s+)?(previous|above|below)\\s+instructions"),
            Pattern.compile("(?i)(?s)forget\\s+(all\\s+)?(previous|above|below)\\s+(instructions|context)"),
            Pattern.compile("(?i)(?s)system\\s*(prompt|message|instruction)"),
            Pattern.compile("(?i)(?s)you\\s+are\\s+(now|not)\\s+(an?\\s+)?(assistant|chatbot|ai|helpful)"),
            Pattern.compile("(?i)(?s)role\\s*(play|take\\s+on\\s+the\\s+role)"),
            Pattern.compile("(?i)(?s)do\\s+not\\s+(follow|adhere|obey|comply)"),
            Pattern.compile("(?i)(?s)bypass\\s+(your\\s+)?(guidelines|restrictions|rules|safety)"),
            Pattern.compile("(?i)(?s)<\\s*(system|user|assistant)\\s*>"),
    };

    private static final int MAX_VARIABLE_LENGTH = 10_000;

    public Map<String, String> sanitizeVariables(Map<String, String> variables) {
        if (variables == null) return Map.of();

        var result = new HashMap<String, String>();
        for (var entry : variables.entrySet()) {
            var sanitized = sanitize(entry.getValue());
            result.put(entry.getKey(), sanitized);
        }
        return result;
    }

    public String sanitize(String input) {
        if (input == null) return "";
        if (input.length() > MAX_VARIABLE_LENGTH) {
            throw new AiInjectionDetectedException("Input exceeds maximum length of " + MAX_VARIABLE_LENGTH);
        }

        for (var pattern : INJECTION_PATTERNS) {
            if (pattern.matcher(input).find()) {
                log.warn("Prompt injection detected: pattern={}", pattern.pattern());
                throw new AiInjectionDetectedException("Input contains prohibited patterns");
            }
        }

        return input;
    }
}
