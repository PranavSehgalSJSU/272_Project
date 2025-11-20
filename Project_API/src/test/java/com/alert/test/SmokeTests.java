package com.alert.test;

///////////////////////////////////////////////////////////////////////////////////////////////////////
//  FILE : SmokeTests.java
//  AUTHOR : Emergency Alert System
//  DESCRIPTION: Smoke tests for core alert system components
///////////////////////////////////////////////////////////////////////////////////////////////////////

import com.alert.audience.AudienceResolver;
import com.alert.condition.ConditionEvaluator;
import com.alert.worker.AlertWorker;
import com.model.Rule;
import com.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class SmokeTests {

    private ConditionEvaluator conditionEvaluator;
    private AudienceResolver audienceResolver;
    private AlertWorker alertWorker;

    @BeforeEach
    void setUp() {
        conditionEvaluator = new ConditionEvaluator();
        audienceResolver = new AudienceResolver();
        alertWorker = new AlertWorker();
    }

    @Test
    void testConditionEvaluator_SimpleConditions() {
        // Test basic mathematical conditions
        Map<String, Object> data = Map.of(
            "temp_c", 42.5,
            "humidity", 15,
            "status", "UP"
        );

        assertTrue(conditionEvaluator.evaluate("temp_c > 40", data));
        assertFalse(conditionEvaluator.evaluate("temp_c < 40", data));
        assertTrue(conditionEvaluator.evaluate("humidity < 20 && temp_c > 40", data));
        assertTrue(conditionEvaluator.evaluate("status === 'UP'", data));
        assertFalse(conditionEvaluator.evaluate("status === 'DOWN'", data));
    }

    @Test
    void testConditionEvaluator_ComplexConditions() {
        Map<String, Object> data = Map.of(
            "temp_c", 35.0,
            "humidity", 85,
            "wind_speed", 25,
            "city", "San Francisco"
        );

        // Test complex conditions
        assertTrue(conditionEvaluator.evaluate("(temp_c > 30 && humidity > 80) || wind_speed > 20", data));
        assertFalse(conditionEvaluator.evaluate("temp_c > 40 && humidity < 50", data));
        assertTrue(conditionEvaluator.evaluate("city === 'San Francisco' && temp_c > 30", data));
    }

    @Test
    void testConditionEvaluator_SafetyChecks() {
        Map<String, Object> data = Map.of("temp", 25);

        // Test that unsafe operations are blocked
        assertFalse(conditionEvaluator.evaluate("System.exit(0)", data));
        assertFalse(conditionEvaluator.evaluate("new java.io.File('test')", data));
        assertFalse(conditionEvaluator.evaluate("Runtime.getRuntime()", data));
        
        // Test that invalid syntax returns false
        assertFalse(conditionEvaluator.evaluate("temp +", data));
        assertFalse(conditionEvaluator.evaluate("invalid syntax here", data));
    }

    @Test 
    void testAudienceResolver_TagFiltering() {
        // Create test users
        List<User> allUsers = Arrays.asList(
            createTestUser("user1", "San Francisco", Arrays.asList("emergency", "weather")),
            createTestUser("user2", "San Jose", Arrays.asList("ops", "engineering")),
            createTestUser("user3", "San Francisco", Arrays.asList("emergency")),
            createTestUser("user4", "Oakland", Arrays.asList("weather", "ops"))
        );

        // Mock the user DAO to return our test users
        audienceResolver = new AudienceResolver() {
            @Override
            public List<User> getAllActiveUsers() {
                return allUsers.stream()
                    .filter(User::isActive)
                    .collect(java.util.stream.Collectors.toList());
            }
        };

        // Test tag-based filtering
        Rule.RuleAudience audience = new Rule.RuleAudience();
        audience.setTags(Arrays.asList("emergency"));
        
        List<User> recipients = audienceResolver.resolveAudience(audience);
        assertEquals(2, recipients.size());
        assertTrue(recipients.stream().allMatch(u -> u.getTags().contains("emergency")));
    }

    @Test
    void testAudienceResolver_CityFiltering() {
        List<User> allUsers = Arrays.asList(
            createTestUser("user1", "San Francisco", Arrays.asList("emergency")),
            createTestUser("user2", "San Jose", Arrays.asList("emergency")), 
            createTestUser("user3", "San Francisco", Arrays.asList("ops")),
            createTestUser("user4", "Oakland", Arrays.asList("emergency"))
        );

        audienceResolver = new AudienceResolver() {
            @Override
            public List<User> getAllActiveUsers() {
                return allUsers.stream()
                    .filter(User::isActive)
                    .toList();
            }
        };

        // Test city-based filtering
        Rule.RuleAudience audience = new Rule.RuleAudience();
        audience.setCity("San Francisco");
        
        List<User> recipients = audienceResolver.resolveAudience(audience);
        assertEquals(2, recipients.size());
        assertTrue(recipients.stream().allMatch(u -> "San Francisco".equals(u.getCity())));
    }

    @Test
    void testAudienceResolver_CombinedFiltering() {
        List<User> allUsers = Arrays.asList(
            createTestUser("user1", "San Francisco", Arrays.asList("emergency", "weather")),
            createTestUser("user2", "San Francisco", Arrays.asList("ops")),
            createTestUser("user3", "San Jose", Arrays.asList("emergency")),
            createTestUser("user4", "San Francisco", Arrays.asList("emergency"))
        );

        audienceResolver = new AudienceResolver() {
            @Override
            public List<User> getAllActiveUsers() {
                return allUsers.stream()
                    .filter(User::isActive)
                    .collect(java.util.stream.Collectors.toList());
            }
        };

        // Test combined city + tag filtering
        Rule.RuleAudience audience = new Rule.RuleAudience();
        audience.setCity("San Francisco");
        audience.setTags(Arrays.asList("emergency"));
        
        List<User> recipients = audienceResolver.resolveAudience(audience);
        assertEquals(2, recipients.size());
        assertTrue(recipients.stream().allMatch(u -> 
            "San Francisco".equals(u.getCity()) && u.getTags().contains("emergency")));
    }

    @Test
    void testRuleFiring_Integration() {
        // Create a test rule
        Rule testRule = new Rule();
        testRule.setId(new org.bson.types.ObjectId("507f1f77bcf86cd799439011"));
        testRule.setName("Test Heat Alert");
        testRule.setSource(Rule.Source.CUSTOM);
        testRule.setCondition("temp_c > 40");
        testRule.setEnabled(true);
        testRule.setCooldownMinutes(5);
        
        Rule.RuleMessage message = new Rule.RuleMessage();
        message.setHeader("Heat Alert");
        message.setContent("Temperature is {{temp_c}}°C in {{city}}");
        message.setChannels(Arrays.asList("email"));
        testRule.setMessage(message);

        Rule.RuleAudience audience = new Rule.RuleAudience();
        audience.setTags(Arrays.asList("emergency"));
        testRule.setAudience(audience);

        // Test data that should trigger the rule
        Map<String, Object> triggeringData = Map.of(
            "temp_c", 42.0,
            "city", "San Francisco"
        );

        // Test the rule evaluation logic
        Map<String, Object> testResult = alertWorker.testRule(testRule, triggeringData, false);
        
        assertNotNull(testResult);
        assertTrue((Boolean) testResult.get("conditionMet"));
        assertNotNull(testResult.get("renderedHeader"));
        assertNotNull(testResult.get("renderedContent"));
        assertEquals("Heat Alert", testResult.get("renderedHeader"));
        assertTrue(((String) testResult.get("renderedContent")).contains("42.0°C"));
    }

    @Test
    void testRuleFiring_ConditionNotMet() {
        Rule testRule = new Rule();
        testRule.setCondition("temp_c > 40");
        testRule.setEnabled(true);
        
        // Test data that should NOT trigger the rule
        Map<String, Object> nonTriggeringData = Map.of(
            "temp_c", 25.0,
            "city", "San Francisco"
        );

        Map<String, Object> testResult = alertWorker.testRule(testRule, nonTriggeringData, false);
        
        assertNotNull(testResult);
        assertFalse((Boolean) testResult.get("conditionMet"));
    }

    // Helper method to create test users
    private User createTestUser(String name, String city, List<String> tags) {
        User user = new User();
        user.setUsername(name);
        user.setEmail(name + "@test.com");
        user.setCity(city);
        user.setTags(tags);
        user.setActive(true);
        return user;
    }
}