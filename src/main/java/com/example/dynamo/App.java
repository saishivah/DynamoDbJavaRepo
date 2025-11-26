package com.example.dynamo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class App {

    public static void main(String[] args) {
        // Init config + repo
        DynamoConfig config = new DynamoConfig();
        UserRepository repo = new UserRepository(config.userTable());

        // 1) Create a new user
        String userId = UUID.randomUUID().toString();

        User newUser = new User();
        newUser.setUserId(userId);
        newUser.setName("Sai");
        newUser.setEmail("sai@example.com");

        repo.insert(newUser);
        System.out.println("Inserted user with id = " + userId);

        // 2) Read by id
        Optional<User> found = repo.getById(userId);
        found.ifPresent(u -> {
            System.out.println("Read from DB:");
            System.out.println(u.getUserId() + " | " + u.getName() + " | " + u.getEmail());
        });

        // 3) Update
        found.ifPresent(u -> {
            u.setName("Sai Updated");
            u.setEmail("sai.updated@example.com");
            repo.update(u);
            System.out.println("Updated user.");
        });

        // 4) Read again after update
        repo.getById(userId).ifPresent(u -> {
            System.out.println("After update:");
            System.out.println(u.getUserId() + " | " + u.getName() + " | " + u.getEmail());
        });

        // 5) List all users
        List<User> users = repo.listAll();
        System.out.println("All users in table:");
        for (User u : users) {
            System.out.println(u.getUserId() + " | " + u.getName() + " | " + u.getEmail());
        }

        // 6) Delete (optional)
//        repo.deleteById(userId);
//        System.out.println("Deleted user with id = " + userId);
    }
}
