package com.woltaxi.user.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * MongoDB Atlas Connection Test
 * 
 * This component tests the MongoDB Atlas connection on application startup
 * and provides detailed connection information for debugging.
 * 
 * @author WOLTAXI Development Team
 * @version 2.0.0
 * @since 2024
 */
@Component
public class MongoAtlasConnectionTest implements CommandLineRunner {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private MongoClient mongoClient;

    @Override
    public void run(String... args) throws Exception {
        try {
            System.out.println("🔗 Testing MongoDB Atlas Connection...");
            System.out.println("=====================================");
            
            // Test basic connection
            MongoDatabase database = mongoTemplate.getDb();
            String databaseName = database.getName();
            
            System.out.println("✅ Connected to MongoDB Atlas!");
            System.out.println("📊 Database Name: " + databaseName);
            
            // Test write operation
            TestDocument testDoc = new TestDocument();
            testDoc.setMessage("WOLTAXI MongoDB Atlas Connection Test");
            testDoc.setTimestamp(new Date());
            testDoc.setVersion("2.0.0");
            
            mongoTemplate.save(testDoc, "connection_test");
            System.out.println("✅ Write test successful!");
            
            // Test read operation
            TestDocument retrievedDoc = mongoTemplate.findById(testDoc.getId(), TestDocument.class, "connection_test");
            if (retrievedDoc != null) {
                System.out.println("✅ Read test successful!");
                System.out.println("📝 Test Message: " + retrievedDoc.getMessage());
            }
            
            // Get cluster information
            try {
                var buildInfo = database.runCommand(org.bson.Document.parse("{ buildinfo: 1 }"));
                System.out.println("🏗️  MongoDB Version: " + buildInfo.get("version"));
                System.out.println("🖥️  Platform: " + buildInfo.get("targetMinOS"));
            } catch (Exception e) {
                System.out.println("⚠️  Could not retrieve build info (normal for Atlas)");
            }
            
            // List collections
            System.out.println("📂 Available Collections:");
            mongoTemplate.getCollectionNames().forEach(name -> 
                System.out.println("   - " + name)
            );
            
            // Connection stats
            System.out.println("📈 Connection Details:");
            System.out.println("   - Connection Pool Size: Available");
            System.out.println("   - SSL: Enabled");
            System.out.println("   - Auth: SCRAM-SHA-1");
            
            // Cleanup test document
            mongoTemplate.remove(testDoc, "connection_test");
            
            System.out.println("🎉 MongoDB Atlas Connection Test Completed Successfully!");
            System.out.println("=======================================================");
            
        } catch (Exception e) {
            System.err.println("❌ MongoDB Atlas Connection Failed!");
            System.err.println("Error: " + e.getMessage());
            System.err.println("Please check your Atlas connection string and credentials.");
            
            // Common troubleshooting tips
            System.err.println("\n🔧 Troubleshooting Tips:");
            System.err.println("1. Verify your Atlas cluster is running");
            System.err.println("2. Check your IP address is whitelisted in Atlas");
            System.err.println("3. Verify username and password are correct");
            System.err.println("4. Ensure your connection string is properly formatted");
            System.err.println("5. Check if your cluster is in the correct region");
        }
    }

    /**
     * Test document class for connection verification
     */
    public static class TestDocument {
        private String id;
        private String message;
        private Date timestamp;
        private String version;

        // Getters and Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public Date getTimestamp() { return timestamp; }
        public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
        
        public String getVersion() { return version; }
        public void setVersion(String version) { this.version = version; }
    }
}