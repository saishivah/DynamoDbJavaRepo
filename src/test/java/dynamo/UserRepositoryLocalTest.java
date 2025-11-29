package dynamo;

import com.example.dynamo.User;
import com.example.dynamo.UserRepository;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;

import software.amazon.dynamodb.services.local.embedded.DynamoDBEmbedded;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserRepositoryLocalTest {

    private static final String TABLE_NAME = "users";

    private DynamoDbClient ddb;                 // embedded v2 client
    private DynamoDbEnhancedClient enhanced;    // enhanced client
    private DynamoDbTable<User> table;          // table handle
    private UserRepository repo;                // your DAO

    @BeforeAll
    void setup() {
        // If you ever hit sqlite4java native-lib errors, uncomment:
        // System.setProperty("sqlite4java.library.path", "native-libs");

        // In-process, in-memory DynamoDB Local (SDK v2 client)
        ddb = DynamoDBEmbedded.create().dynamoDbClient();

        enhanced = DynamoDbEnhancedClient.builder()
                .dynamoDbClient(ddb)
                .build();

        table = enhanced.table(TABLE_NAME, TableSchema.fromBean(User.class));
        repo = new UserRepository(table);
    }

    @AfterAll
    void shutdown() {
        if (ddb != null) {
            ddb.close();   // closes embedded local client
        }
    }

    @BeforeEach
    void resetTable() {
        // Drop & recreate table for a clean slate each test
        try {
            table.deleteTable();
        } catch (ResourceNotFoundException ignored) {
            // table might not exist yet, that's fine
        }
        table.createTable();
    }

    // ----------------- Tests -----------------

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
