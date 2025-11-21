package com.persistance.Rule;
///////////////////////////////////////////////////////////////////////////////////////////////////////
//  FILE : RuleFileDAO.java
//  AUTHOR : Emergency Alert System
//  DESCRIPTION: MongoDB implementation for Rule data access
///////////////////////////////////////////////////////////////////////////////////////////////////////

import com.model.Rule;
import com.persistance.Database.MongoConn;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.mongodb.client.model.Filters.*;

public class RuleFileDAO implements RuleDAO {
    
    private final MongoCollection<Document> collection;
    
    public RuleFileDAO() {
        MongoDatabase database = MongoConn.getDatabase();
        this.collection = database != null ? database.getCollection("rules") : null;
    }
    
    @Override
    public Rule createRule(Rule rule) {
        if (collection == null) {
            System.err.println("Database not connected - cannot create rule");
            return null;
        }
        try {
            Document doc = rule.getDoc();
            collection.insertOne(doc);
            rule.setId(doc.getObjectId("_id"));
            return rule;
        } catch (Exception e) {
            System.err.println("Error creating rule: " + e.getMessage());
            return null;
        }
    }
    
    @Override
    public Rule updateRule(Rule rule) {
        if (collection == null) {
            System.err.println("Database not connected - cannot update rule");
            return null;
        }
        try {
            Document doc = rule.getDoc();
            collection.replaceOne(eq("_id", rule.getId()), doc);
            return rule;
        } catch (Exception e) {
            System.err.println("Error updating rule: " + e.getMessage());
            return null;
        }
    }
    
    @Override
    public Rule getRuleById(ObjectId id) {
        if (collection == null) {
            System.err.println("Database not connected - cannot get rule by ID");
            return null;
        }
        try {
            Document doc = collection.find(eq("_id", id)).first();
            return doc != null ? documentToRule(doc) : null;
        } catch (Exception e) {
            System.err.println("Error getting rule by ID: " + e.getMessage());
            return null;
        }
    }
    
    @Override
    public Rule getRuleById(String id) {
        try {
            return getRuleById(new ObjectId(id));
        } catch (Exception e) {
            System.err.println("Error getting rule by string ID: " + e.getMessage());
            return null;
        }
    }
    
    @Override
    public List<Rule> getAllRules() {
        if (collection == null) {
            System.err.println("Database not connected - returning empty rules list");
            return new ArrayList<>();
        }
        try {
            List<Rule> rules = new ArrayList<>();
            for (Document doc : collection.find()) {
                Rule rule = documentToRule(doc);
                if (rule != null) {
                    rules.add(rule);
                }
            }
            return rules;
        } catch (Exception e) {
            System.err.println("Error getting all rules: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    @Override
    public List<Rule> getEnabledRules() {
        try {
            List<Rule> rules = new ArrayList<>();
            for (Document doc : collection.find(eq("enabled", true))) {
                Rule rule = documentToRule(doc);
                if (rule != null) {
                    rules.add(rule);
                }
            }
            return rules;
        } catch (Exception e) {
            System.err.println("Error getting enabled rules: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    @Override
    public boolean deleteRule(ObjectId id) {
        try {
            return collection.deleteOne(eq("_id", id)).getDeletedCount() > 0;
        } catch (Exception e) {
            System.err.println("Error deleting rule: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean deleteRule(String id) {
        try {
            return deleteRule(new ObjectId(id));
        } catch (Exception e) {
            System.err.println("Error deleting rule by string ID: " + e.getMessage());
            return false;
        }
    }
    
    private Rule documentToRule(Document doc) {
        try {
            Rule rule = new Rule();
            rule.setId(doc.getObjectId("_id"));
            rule.setName(doc.getString("name"));
            rule.setSource(Rule.Source.valueOf(doc.getString("source")));
            rule.setParams((Map<String, Object>) doc.get("params", Map.class));
            rule.setCondition(doc.getString("condition"));
            rule.setCooldownMinutes(doc.getInteger("cooldownMinutes", 60));
            rule.setEnabled(doc.getBoolean("enabled", true));
            
            // Parse message
            Document messageDoc = (Document) doc.get("message");
            if (messageDoc != null) {
                Rule.RuleMessage message = new Rule.RuleMessage();
                message.setHeader(messageDoc.getString("header"));
                message.setContent(messageDoc.getString("content"));
                message.setChannels(messageDoc.getList("channels", String.class));
                rule.setMessage(message);
            }
            
            // Parse audience
            Document audienceDoc = (Document) doc.get("audience");
            if (audienceDoc != null) {
                Rule.RuleAudience audience = new Rule.RuleAudience();
                audience.setTags(audienceDoc.getList("tags", String.class));
                audience.setCity(audienceDoc.getString("city"));
                rule.setAudience(audience);
            }
            
            // Parse lastFiredAt
            String lastFiredStr = doc.getString("lastFiredAt");
            if (lastFiredStr != null && !lastFiredStr.isEmpty()) {
                rule.setLastFiredAt(LocalDateTime.parse(lastFiredStr));
            }
            
            return rule;
        } catch (Exception e) {
            System.err.println("Error converting document to rule: " + e.getMessage());
            return null;
        }
    }
}