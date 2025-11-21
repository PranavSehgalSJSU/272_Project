package com.alert.template;
///////////////////////////////////////////////////////////////////////////////////////////////////////
//  FILE : MessageTemplater.java
//  AUTHOR : Emergency Alert System
//  DESCRIPTION: Message templating system with variable replacement
///////////////////////////////////////////////////////////////////////////////////////////////////////

import com.model.Rule;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class MessageTemplater {
    
    private static final Pattern TEMPLATE_VARIABLE = Pattern.compile("\\{\\{([a-zA-Z_][a-zA-Z0-9_]*)\\}\\}");
    
    /**
     * Render a message template with data variables
     * @param message The rule message template
     * @param data The data map for variable substitution
     * @return Rendered message with variables replaced
     */
    public RenderedMessage renderMessage(Rule.RuleMessage message, Map<String, Object> data) {
        if (message == null) {
            throw new RuntimeException("Message template is null");
        }
        
        String renderedHeader = renderTemplate(message.getHeader(), data);
        String renderedContent = renderTemplate(message.getContent(), data);
        
        return new RenderedMessage(renderedHeader, renderedContent, message.getChannels());
    }
    
    /**
     * Render a template string by replacing {{variable}} placeholders
     * @param template The template string
     * @param data The data map for variable substitution
     * @return Rendered string with variables replaced
     */
    public String renderTemplate(String template, Map<String, Object> data) {
        if (template == null || template.isEmpty()) {
            return template;
        }
        
        if (data == null || data.isEmpty()) {
            // Return template with variables removed if no data
            return TEMPLATE_VARIABLE.matcher(template).replaceAll("");
        }
        
        StringBuffer result = new StringBuffer();
        Matcher matcher = TEMPLATE_VARIABLE.matcher(template);
        
        while (matcher.find()) {
            String variableName = matcher.group(1);
            String replacement = getVariableValue(variableName, data);
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);
        
        return result.toString();
    }
    
    private String getVariableValue(String variableName, Map<String, Object> data) {
        Object value = data.get(variableName);
        
        if (value == null) {
            System.err.println("Warning: Template variable not found in data: " + variableName);
            return "[" + variableName + "]"; // Show missing variable clearly
        }
        
        return value.toString();
    }
    
    /**
     * Validate that all variables in the template exist in the data
     * @param template The template string to validate
     * @param data The data map
     * @return true if all variables can be resolved
     */
    public boolean validateTemplate(String template, Map<String, Object> data) {
        if (template == null || template.isEmpty()) {
            return true;
        }
        
        Matcher matcher = TEMPLATE_VARIABLE.matcher(template);
        while (matcher.find()) {
            String variableName = matcher.group(1);
            if (!data.containsKey(variableName)) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Extract all variable names from a template
     * @param template The template string
     * @return Array of variable names found in the template
     */
    public String[] extractVariables(String template) {
        if (template == null || template.isEmpty()) {
            return new String[0];
        }
        
        Matcher matcher = TEMPLATE_VARIABLE.matcher(template);
        return matcher.results()
                .map(match -> match.group(1))
                .distinct()
                .toArray(String[]::new);
    }
    
    /**
     * Rendered message container
     */
    public static class RenderedMessage {
        private final String header;
        private final String content;
        private final java.util.List<String> channels;
        
        public RenderedMessage(String header, String content, java.util.List<String> channels) {
            this.header = header;
            this.content = content;
            this.channels = channels;
        }
        
        public String getHeader() { return header; }
        public String getContent() { return content; }
        public java.util.List<String> getChannels() { return channels; }
        
        @Override
        public String toString() {
            return String.format("RenderedMessage{header='%s', content='%s', channels=%s}", 
                               header, content, channels);
        }
    }
}