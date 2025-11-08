///////////////////////////////////////////////////////////////////////////////////////////////////////
package com.persistance.Database;
//  FILE : MongoDBConn.java
//  AUTHOR : Pranav Sehgal <PranavSehgalSJSU>
//  DESCRIPTION: Is main mesh layer used to connect to MongoDB atlas connection instance
///////////////////////////////////////////////////////////////////////////////////////////////////////
import org.springframework.context.annotation.Configuration;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

@Configuration
public class MongoConn {
    private static MongoDatabase database = null;


    private static String conStr = PropertyReader.getProperty("mongo.url");
    private static String dbName = PropertyReader.getProperty("mongo.dbn");;

    public static MongoDatabase getDatabase() {
        if (database == null) {
            try{
                MongoClient client = MongoClients.create(conStr);
                database = client.getDatabase(dbName);
            }catch(Exception e){
                System.out.println("\n\nUnable to Connect to MongoDb");
            }
        }
        return database;
    }
}
