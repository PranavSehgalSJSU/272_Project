package com.persistance.Rule;
///////////////////////////////////////////////////////////////////////////////////////////////////////
//  FILE : RuleDAO.java
//  AUTHOR : Emergency Alert System
//  DESCRIPTION: Interface for Rule data access operations
///////////////////////////////////////////////////////////////////////////////////////////////////////

import com.model.Rule;
import org.bson.types.ObjectId;
import java.util.List;

public interface RuleDAO {
    Rule createRule(Rule rule);
    Rule updateRule(Rule rule);
    Rule getRuleById(ObjectId id);
    Rule getRuleById(String id);
    List<Rule> getAllRules();
    List<Rule> getEnabledRules();
    boolean deleteRule(ObjectId id);
    boolean deleteRule(String id);
}