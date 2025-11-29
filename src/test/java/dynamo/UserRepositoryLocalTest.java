package dynamo;

import com.example.dynamo.User;
import com.example.dynamo.UserRepository;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableRequest;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.ProvisionedThroughput;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;

import software.amazon.dynamodb.services.local.main.ServerRunner;
import software.amazon.dynamodb.services.local.server.DynamoDBProxyServer;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserRepositoryLocalTest {

    private static final String TABLE_NAME = "users";

    private DynamoDBProxyServer server;
    private LocalDynamoConfig config;
    private DynamoDbClient ddb;
    private UserRepository repo;

    @BeforeAll
    void startDbAndSetupClient() throws Exception {
        // Start embedded DynamoDB Local in-memory on port 8000
        server = ServerRunner.createServerFromCommandLineArgs(
                new String[] { "-inMemory", "-port", "8000" }
        );
        server.start();

        // Configure AWS SDK v2 client pointing at local DynamoDB
        config = new LocalDynamoConfig();
        ddb = config.lowLevel();

        // Ensure the "users" table exists
        createTableIfNotExists();

        // Create repository using enhanced client table
        repo = new UserRepository(config.userTable());
    }

    @AfterAll
    void stopDb() throws Exception {
        if (server != null) {
            server.stop();
        }
    }

    @BeforeEach
    void cleanTable() {
        // Delete all items from the table before each test
        ScanResponse scan = ddb.scan(ScanRequest.builder()
                .tableName(TABLE_NAME)
                .build());

        for (var item : scan.items()) {
            ddb.deleteItem(DeleteItemRequest.builder()
                    .tableName(TABLE_NAME)
                    .key(Map.of(
                            "userId", item.get("userId") // primary key only
                    ))
                    .build());
        }
    }

    private void createTableIfNotExists() {
        // Check if table exists
        try {
            ddb.describeTable(DescribeTableRequest.builder()
                    .tableName(TABLE_NAME)
                    .build());
            return; // already exists
        } catch (ResourceNotFoundException ignored) {
            // fall through and create
        }

        // Create table
        ddb.createTable(CreateTableRequest.builder()
                .tableName(TABLE_NAME)
                .keySchema(KeySchemaElement.builder()
                        .attributeName("userId")
                        .keyType(KeyType.HASH)
                        .build())
                .attributeDefinitions(AttributeDefinition.builder()
                        .attributeName("userId")
                        .attributeType(ScalarAttributeType.S)
                        .build())
                .provisionedThroughput(ProvisionedThroughput.builder()
                        .readCapacityUnits(5L)
                        .writeCapacityUnits(5L)
                        .build())
                .build());

        // Wait until table is active
        ddb.waiter().waitUntilTableExists(
                DescribeTableRequest.builder()
                        .tableName(TABLE_NAME)
                        .build()
        );
    }

    @Test
    void insertAndGetById() {
        String id = UUID.randomUUID().toString();

        User u = new User();
        u.setUserId(id);
        u.setName("Sai");
        u.setEmail("sai@example.com");

        repo.insert(u);

        User fromDb = repo.getById(id).orElseThrow();
        assertEquals("Sai", fromDb.getName());
        assertEquals("sai@example.com", fromDb.getEmail());
    }

    @Test
    void listAllReturnsAll() {
        for (int i = 0; i < 3; i++) {
            User u = new User();
            u.setUserId(UUID.randomUUID().toString());
            u.setName("User " + i);
            u.setEmail("u" + i + "@example.com");
            repo.insert(u);
        }

        List<User> all = repo.listAll();
        assertEquals(3, all.size());
        assertTrue(all.stream().allMatch(u -> u.getEmail().endsWith("@example.com")));
    }
}
