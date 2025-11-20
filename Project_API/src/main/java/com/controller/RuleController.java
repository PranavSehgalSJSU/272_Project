package com.controller;
///////////////////////////////////////////////////////////////////////////////////////////////////////
//  FILE : RuleController.java
//  AUTHOR : Emergency Alert System
//  DESCRIPTION: REST controller for rule management
///////////////////////////////////////////////////////////////////////////////////////////////////////

import com.alert.worker.AlertWorker;
import com.dto.RuleDTO;
import com.model.Rule;
import com.persistance.Rule.RuleDAO;
import com.persistance.Rule.RuleFileDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/rules")
@CrossOrigin(origins = "*")
public class RuleController {
    
    private final RuleDAO ruleDAO;
    private final AlertWorker alertWorker;
    
    @Autowired
    public RuleController(AlertWorker alertWorker) {
        this.ruleDAO = new RuleFileDAO();
        this.alertWorker = alertWorker;
    }
    
    @GetMapping
    public ResponseEntity<List<RuleDTO>> getAllRules() {
        try {
            List<Rule> rules = ruleDAO.getAllRules();
            List<RuleDTO> ruleDTOs = rules.stream()
                .map(RuleDTO::new)
                .collect(Collectors.toList());
            return ResponseEntity.ok(ruleDTOs);
        } catch (Exception e) {
            System.err.println("Error getting rules: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<RuleDTO> getRuleById(@PathVariable String id) {
        try {
            Rule rule = ruleDAO.getRuleById(id);
            if (rule != null) {
                return ResponseEntity.ok(new RuleDTO(rule));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            System.err.println("Error getting rule by ID: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PostMapping
    public ResponseEntity<RuleDTO> createRule(@RequestBody Rule rule) {
        try {
            // Validate required fields
            if (rule.getName() == null || rule.getName().trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            if (rule.getSource() == null) {
                return ResponseEntity.badRequest().build();
            }
            if (rule.getCondition() == null || rule.getCondition().trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            
            Rule createdRule = ruleDAO.createRule(rule);
            if (createdRule != null) {
                return ResponseEntity.status(HttpStatus.CREATED).body(new RuleDTO(createdRule));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        } catch (Exception e) {
            System.err.println("Error creating rule: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<RuleDTO> updateRule(@PathVariable String id, @RequestBody Rule rule) {
        try {
            Rule existingRule = ruleDAO.getRuleById(id);
            if (existingRule == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Set the ID to ensure we're updating the right rule
            rule.setId(existingRule.getId());
            
            Rule updatedRule = ruleDAO.updateRule(rule);
            if (updatedRule != null) {
                return ResponseEntity.ok(new RuleDTO(updatedRule));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        } catch (Exception e) {
            System.err.println("Error updating rule: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<RuleDTO> toggleRule(@PathVariable String id) {
        try {
            Rule rule = ruleDAO.getRuleById(id);
            if (rule == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Toggle enabled status
            rule.setEnabled(!rule.isEnabled());
            Rule updatedRule = ruleDAO.updateRule(rule);
            
            if (updatedRule != null) {
                return ResponseEntity.ok(new RuleDTO(updatedRule));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        } catch (Exception e) {
            System.err.println("Error toggling rule: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRule(@PathVariable String id) {
        try {
            boolean deleted = ruleDAO.deleteRule(id);
            if (deleted) {
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            System.err.println("Error deleting rule: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PostMapping("/{id}/test")
    public ResponseEntity<Map<String, Object>> testRule(@PathVariable String id, 
                                                       @RequestBody(required = false) Map<String, Object> testRequest) {
        try {
            Rule rule = ruleDAO.getRuleById(id);
            if (rule == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Extract test parameters
            Map<String, Object> mockData = null;
            boolean actuallyFire = false;
            
            if (testRequest != null) {
                mockData = (Map<String, Object>) testRequest.get("mockData");
                actuallyFire = Boolean.TRUE.equals(testRequest.get("send"));
            }
            
            Map<String, Object> testResult = alertWorker.testRule(rule, mockData, actuallyFire);
            return ResponseEntity.ok(testResult);
            
        } catch (Exception e) {
            System.err.println("Error testing rule: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}