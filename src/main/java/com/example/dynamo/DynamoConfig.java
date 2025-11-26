package com.example.dynamo;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

public class DynamoConfig {

    private static final String TABLE_NAME = "users";

    private final DynamoDbClient dynamoDbClient;
    private final DynamoDbEnhancedClient enhancedClient;

    public DynamoConfig() {
        // Uses credentials from aws configure / env vars / instance profile
        this.dynamoDbClient = DynamoDbClient.builder()
                .region(Region.US_EAST_1) // change if needed
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();

        this.enhancedClient = DynamoDbEnhancedClient.builder()
                .dynamoDbClient(dynamoDbClient)
                .build();
    }

    public DynamoDbEnhancedClient enhancedClient() {
        return enhancedClient;
    }

    public DynamoDbTable<User> userTable() {
        return enhancedClient.table(TABLE_NAME, TableSchema.fromBean(User.class));
    }
}
