package dynamo;

import com.example.dynamo.User;
import com.example.dynamo.UserRepository;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;

import static org.junit.jupiter.api.Assertions.*;
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserRepositoryLocalTest {

    private static final String TABLE_NAME = "users";

    private LocalDynamoConfig config;
    private DynamoDbClient ddb;
    private UserRepository repo;

    @BeforeAll
    void setupAll() {
        config = new LocalDynamoConfig();
        ddb = config.lowLevel();
        createTableIfNotExists();
        repo = new UserRepository(config.userTable());
    }


    @BeforeEach
    void cleanTable() {
        ScanResponse scan = ddb.scan(ScanRequest.builder()
                .tableName(TABLE_NAME)
                .build());

        for (var item : scan.items()) {
            ddb.deleteItem(DeleteItemRequest.builder()
                    .tableName(TABLE_NAME)
                    .key(Map.of(
                            "userId", item.get("userId") // only the key attribute
                    ))
                    .build());
        }
    }

    private void createTableIfNotExists() {
        try {
            ddb.describeTable(DescribeTableRequest.builder()
                    .tableName(TABLE_NAME)
                    .build());
            return; // already exists
        } catch (ResourceNotFoundException ignored) {}

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

        ddb.waiter().waitUntilTableExists(
                DescribeTableRequest.builder().tableName(TABLE_NAME).build()
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
    }
}
