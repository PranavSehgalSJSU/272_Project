package com.alert.source;
///////////////////////////////////////////////////////////////////////////////////////////////////////
//  FILE : SourceAdapter.java
//  AUTHOR : Emergency Alert System
//  DESCRIPTION: Interface for data source adapters
///////////////////////////////////////////////////////////////////////////////////////////////////////

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface SourceAdapter {
    /**
     * Fetch data from the source using the provided parameters
     * @param params Configuration parameters for the source
     * @return CompletableFuture containing a map of normalized data
     */
    CompletableFuture<Map<String, Object>> fetch(Map<String, Object> params);
    
    /**
     * Get the source type identifier
     * @return String identifier for this source type
     */
    String getSourceType();
}