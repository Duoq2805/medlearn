package com.duoq.medlearn.ai.prompt;

import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;

@Service
public class VariablesExtractor {

    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{\\{\\s*(\\w+)\\s*}}");

    public Set<String> extract(String template) {
        var variables = new LinkedHashSet<String>();
        if (template == null) return variables;

        var matcher = VARIABLE_PATTERN.matcher(template);
        while (matcher.find()) {
            variables.add(matcher.group(1));
        }
        return variables;
    }
}
