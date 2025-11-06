package com.persistance.Database;
///////////////////////////////////////////////////////////////////////////////////////////////////////
//  FILE : PropertyReader.java
//  AUTHOR : Pranav Sehgal <PranavSehgalSJSU>
//  DESCRIPTION: I AM TOO INFURIATED TO DEAL WITH @VALUE, SO JUST MADE THIS INSTEAD
///////////////////////////////////////////////////////////////////////////////////////////////////////

import java.io.InputStream;
import java.util.Properties;

public class PropertyReader {

    public static String getProperty(String key) {
        try (InputStream input  = PropertyReader.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (input == null){ return null; }
            Properties props = new Properties();
            props.load(input);
            return props.getProperty(key);
        } catch (Exception e) {
            return null;
        }
    }
}
