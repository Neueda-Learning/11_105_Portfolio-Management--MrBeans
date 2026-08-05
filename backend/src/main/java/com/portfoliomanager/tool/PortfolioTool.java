package com.portfoliomanager.tool;

import java.util.Map;

public interface PortfolioTool {
    String getName();
    String getDescription();
    
    /**
     * Returns a JSON schema representing the expected parameters.
     * Use standard JSON schema (type: object, properties: {...}).
     */
    Map<String, Object> getParameterSchema();
    
    Object execute(Map<String, Object> args);
}
