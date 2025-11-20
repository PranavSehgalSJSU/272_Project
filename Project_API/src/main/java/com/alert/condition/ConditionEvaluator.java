package com.alert.condition;
///////////////////////////////////////////////////////////////////////////////////////////////////////
//  FILE : ConditionEvaluator.java
//  AUTHOR : Emergency Alert System
//  DESCRIPTION: Safe expression evaluator for rule conditions
///////////////////////////////////////////////////////////////////////////////////////////////////////

import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class ConditionEvaluator {
    
    // Whitelist patterns for safe evaluation
    private static final Pattern SAFE_VARIABLE = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*$");
    private static final Pattern SAFE_NUMBER = Pattern.compile("^-?\\d+(\\.\\d+)?$");
    private static final Pattern SAFE_STRING = Pattern.compile("^\"[^\"]*\"$|^'[^']*'$");
    
    // Allowed operators
    private static final Pattern COMPARISON_OPS = Pattern.compile("(>=|<=|!=|==|>|<)");
    private static final Pattern LOGICAL_OPS = Pattern.compile("(&&|\\|\\|)");
    
    /**
     * Safely evaluate a condition expression against provided data
     * @param condition The condition string (e.g., "temp_c > 40 && humidity < 20")
     * @param data The data map to evaluate against
     * @return true if condition is met, false otherwise
     * @throws RuntimeException if condition is unsafe or invalid
     */
    public boolean evaluate(String condition, Map<String, Object> data) {
        if (condition == null || condition.trim().isEmpty()) {
            return true; // Empty condition means always true
        }
        
        // Sanitize and validate the condition
        String sanitized = sanitizeCondition(condition.trim());
        validateCondition(sanitized, data);
        
        // Parse and evaluate
        return evaluateExpression(sanitized, data);
    }
    
    private String sanitizeCondition(String condition) {
        // Remove extra whitespace and normalize
        return condition.replaceAll("\\s+", " ").trim();
    }
    
    private void validateCondition(String condition, Map<String, Object> data) {
        // Split by logical operators to get individual comparisons
        String[] parts = condition.split("\\s*(&&|\\|\\|)\\s*");
        
        for (String part : parts) {
            validateComparison(part.trim(), data);
        }
    }
    
    private void validateComparison(String comparison, Map<String, Object> data) {
        // Match pattern: variable operator value
        Pattern comparisonPattern = Pattern.compile("^\\s*([a-zA-Z_][a-zA-Z0-9_]*)\\s*(>=|<=|!=|==|>|<)\\s*(.+)\\s*$");
        Matcher matcher = comparisonPattern.matcher(comparison);
        
        if (!matcher.matches()) {
            throw new RuntimeException("Invalid comparison format: " + comparison);
        }
        
        String variable = matcher.group(1);
        String operator = matcher.group(2);
        String value = matcher.group(3);
        
        // Validate variable exists in data
        if (!data.containsKey(variable)) {
            throw new RuntimeException("Variable not found in data: " + variable);
        }
        
        // Validate variable name is safe
        if (!SAFE_VARIABLE.matcher(variable).matches()) {
            throw new RuntimeException("Unsafe variable name: " + variable);
        }
        
        // Validate value is safe (number or quoted string)
        if (!SAFE_NUMBER.matcher(value).matches() && !SAFE_STRING.matcher(value).matches()) {
            throw new RuntimeException("Unsafe value: " + value);
        }
    }
    
    private boolean evaluateExpression(String condition, Map<String, Object> data) {
        try {
            // Handle logical operators by splitting and evaluating parts
            if (condition.contains("&&")) {
                String[] parts = condition.split("\\s*&&\\s*");
                for (String part : parts) {
                    if (!evaluateComparison(part.trim(), data)) {
                        return false;
                    }
                }
                return true;
            } else if (condition.contains("||")) {
                String[] parts = condition.split("\\s*\\|\\|\\s*");
                for (String part : parts) {
                    if (evaluateComparison(part.trim(), data)) {
                        return true;
                    }
                }
                return false;
            } else {
                // Single comparison
                return evaluateComparison(condition, data);
            }
        } catch (Exception e) {
            System.err.println("Error evaluating condition: " + condition + " - " + e.getMessage());
            return false;
        }
    }
    
    private boolean evaluateComparison(String comparison, Map<String, Object> data) {
        Pattern comparisonPattern = Pattern.compile("^\\s*([a-zA-Z_][a-zA-Z0-9_]*)\\s*(>=|<=|!=|==|>|<)\\s*(.+)\\s*$");
        Matcher matcher = comparisonPattern.matcher(comparison);
        
        if (!matcher.matches()) {
            return false;
        }
        
        String variable = matcher.group(1);
        String operator = matcher.group(2);
        String valueStr = matcher.group(3);
        
        Object dataValue = data.get(variable);
        if (dataValue == null) {
            return false;
        }
        
        // Parse the comparison value
        Object comparisonValue = parseValue(valueStr);
        
        return performComparison(dataValue, operator, comparisonValue);
    }
    
    private Object parseValue(String valueStr) {
        if (SAFE_NUMBER.matcher(valueStr).matches()) {
            if (valueStr.contains(".")) {
                return Double.parseDouble(valueStr);
            } else {
                return Long.parseLong(valueStr);
            }
        } else if (SAFE_STRING.matcher(valueStr).matches()) {
            // Remove quotes
            return valueStr.substring(1, valueStr.length() - 1);
        }
        throw new RuntimeException("Unable to parse value: " + valueStr);
    }
    
    private boolean performComparison(Object dataValue, String operator, Object comparisonValue) {
        // Handle string comparisons
        if (dataValue instanceof String || comparisonValue instanceof String) {
            String dataStr = dataValue.toString();
            String compStr = comparisonValue.toString();
            
            switch (operator) {
                case "==": return dataStr.equals(compStr);
                case "!=": return !dataStr.equals(compStr);
                default: throw new RuntimeException("String comparison only supports == and != operators");
            }
        }
        
        // Handle numeric comparisons
        double dataNum = ((Number) dataValue).doubleValue();
        double compNum = ((Number) comparisonValue).doubleValue();
        
        switch (operator) {
            case "==": return dataNum == compNum;
            case "!=": return dataNum != compNum;
            case ">": return dataNum > compNum;
            case "<": return dataNum < compNum;
            case ">=": return dataNum >= compNum;
            case "<=": return dataNum <= compNum;
            default: throw new RuntimeException("Unknown operator: " + operator);
        }
    }
}