///////////////////////////////////////////////////////////////////////////////////////////////////////
package com.persistance.Database;
//  FILE : MongoDBConn.java
//  AUTHOR : Pranav Sehgal <PranavSehgalSJSU>
//  DESCRIPTION: Is main mesh layer used to connect to MongoDB atlas connection instance
///////////////////////////////////////////////////////////////////////////////////////////////////////
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MongoConn {
    private static MongoDatabase database = null;


    private static String conStr = PropertyReader.getProperty("mongo.url");
    private static String dbName = PropertyReader.getProperty("mongo.dbn");;

    public static MongoDatabase getDatabase() {
        if (database == null) {
            MongoClient client = MongoClients.create(conStr);
            database = client.getDatabase(dbName);
        }
        return database;
    }
}
