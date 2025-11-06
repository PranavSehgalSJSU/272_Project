///////////////////////////////////////////////////////////////////////////////////////////////////////
package com.persistance.Database;
//  FILE : MongoDBConn.java
//  AUTHOR : Pranav Sehgal <PranavSehgalSJSU>
//  DESCRIPTION: Is main mesh layer used to connect to MongoDB atlas connection instance
///////////////////////////////////////////////////////////////////////////////////////////////////////
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

public class MongoDBConn {
    private static MongoDatabase database = null;
    private static final String CONNECTION_STRING = "mongodb+srv://DB_USER:DB_PASS@cluster0.pfxzed6.mongodb.net/?appName=Cluster0";
    private static final String DB_NAME = "notification_system";

    public static MongoDatabase getDatabase() {
        if (database == null) {
            MongoClient client = MongoClients.create(CONNECTION_STRING);
            database = client.getDatabase(DB_NAME);
        }
        return database;
    }
}
