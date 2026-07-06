package com.duoq.medlearn.ai.security;

import com.duoq.medlearn.ai.exception.AiOutputFilteredException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;

@Service
@Slf4j
public class AiOutputValidator {

    private static final List<Pattern> BLOCKED_PATTERNS = List.of(
            Pattern.compile("(?i)I am (an? )?AI (language model|assistant)"),
            Pattern.compile("(?i)As an? (AI|artificial intelligence)"),
            Pattern.compile("(?i)As a large language model")
    );

    public String validate(String output) {
        if (output == null) return "";

        for (var pattern : BLOCKED_PATTERNS) {
            if (pattern.matcher(output).find()) {
                log.warn("AI output filtered: matched pattern={}", pattern.pattern());
                throw new AiOutputFilteredException("Output was filtered due to content policy");
            }
        }

        return output;
    }
}
