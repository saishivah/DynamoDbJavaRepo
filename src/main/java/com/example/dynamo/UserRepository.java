package com.example.dynamo;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserRepository {

    private final DynamoDbTable<User> userTable;

    public UserRepository(DynamoDbTable<User> userTable) {
        this.userTable = userTable;
    }

    // CREATE/INSERT (also works as upsert)
    public void insert(User user) {
        userTable.putItem(user);
    }

    // READ by id
    public Optional<User> getById(String userId) {
        User key = new User();
        key.setUserId(userId);

        User found = userTable.getItem(key);
        return Optional.ofNullable(found);
    }

    // UPDATE (full overwrite)
    public void update(User user) {
        userTable.putItem(user);
    }

    // DELETE by id
    public void deleteById(String userId) {
        User key = new User();
        key.setUserId(userId);
        userTable.deleteItem(key);
    }

    // LIST all users (scan)
    public List<User> listAll() {
        List<User> result = new ArrayList<>();

        PageIterable<User> pages = userTable.scan();
        for (Page<User> page : pages) {
            result.addAll(page.items());
        }

        return result;
    }
}
