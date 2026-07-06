package com.duoq.medlearn.ai.prompt;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class PromptRenderer {

    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{\\{\\s*(\\w+)\\s*}}");

    public String render(String template, Map<String, String> variables) {
        if (template == null) return "";

        var result = VARIABLE_PATTERN.matcher(template);
        var sb = new StringBuffer();

        while (result.find()) {
            var varName = result.group(1);
            var value = variables != null ? variables.getOrDefault(varName, "") : "";
            result.appendReplacement(sb, Matcher.quoteReplacement(value));
        }
        result.appendTail(sb);

        return sb.toString();
    }

    public boolean hasUnresolvedVariables(String text) {
        return VARIABLE_PATTERN.matcher(text).find();
    }
}
