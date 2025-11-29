package dynamo;

import java.net.URI;

import com.example.dynamo.User;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;


public class LocalDynamoConfig {

    private static final String TABLE_NAME = "users";

    private final DynamoDbClient client;
    private final DynamoDbEnhancedClient enhanced;

    public LocalDynamoConfig() {
        this.client = DynamoDbClient.builder()
                .endpointOverride(URI.create("http://localhost:8000")) // DynamoDB Local
                .region(Region.US_EAST_1)
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create("DUMMYIDEXAMPLE", "DUMMYEXAMPLEKEY")
                        )
                )
                .build();

        this.enhanced = DynamoDbEnhancedClient.builder()
                .dynamoDbClient(client)
                .build();
    }

    public DynamoDbClient lowLevel() {
        return client;
    }

    public DynamoDbTable<User> userTable() {
        return enhanced.table(TABLE_NAME, TableSchema.fromBean(User.class));
    }
}
