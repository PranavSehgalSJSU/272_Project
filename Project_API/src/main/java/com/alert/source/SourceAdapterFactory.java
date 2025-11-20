package com.alert.source;
///////////////////////////////////////////////////////////////////////////////////////////////////////
//  FILE : SourceAdapterFactory.java
//  AUTHOR : Emergency Alert System
//  DESCRIPTION: Factory for creating source adapters
///////////////////////////////////////////////////////////////////////////////////////////////////////

import com.model.Rule;
import java.util.HashMap;
import java.util.Map;

public class SourceAdapterFactory {
    private static final Map<Rule.Source, SourceAdapter> adapters = new HashMap<>();
    
    static {
        adapters.put(Rule.Source.WEATHER, new WeatherSourceAdapter());
        adapters.put(Rule.Source.STATUS, new StatusSourceAdapter());
    }
    
    public static SourceAdapter getAdapter(Rule.Source source) {
        SourceAdapter adapter = adapters.get(source);
        if (adapter == null) {
            throw new RuntimeException("No adapter found for source: " + source);
        }
        return adapter;
    }
    
    public static boolean isSourceSupported(Rule.Source source) {
        return adapters.containsKey(source);
    }
}