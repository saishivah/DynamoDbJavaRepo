package com.example.dynamo;

import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

public class DynamoConfig {

    private static final String TABLE_NAME = "users";

    private final DynamoDbClient dynamoDbClient;
    private final DynamoDbEnhancedClient enhancedClient;

    private final String profileName;
    private final Region region;

    // --- NEW: constructor with profile ---
    public DynamoConfig(String profileName, Region region) {
        this.profileName = profileName;
        this.region = region;

        this.dynamoDbClient = DynamoDbClient.builder()
                .region(region)
                .credentialsProvider(
                        ProfileCredentialsProvider.create(profileName)
                )
                .build();

        this.enhancedClient = DynamoDbEnhancedClient.builder()
                .dynamoDbClient(dynamoDbClient)
                .build();
    }

    // Convenience constructor (defaults to your usual settings)
    public DynamoConfig() {
        this("test", Region.US_EAST_1);
    }

    public DynamoDbEnhancedClient enhancedClient() {
        return enhancedClient;
    }

    public DynamoDbTable<User> userTable() {
        return enhancedClient.table(TABLE_NAME, TableSchema.fromBean(User.class));
    }
}
