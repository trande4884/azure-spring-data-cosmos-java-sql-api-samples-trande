// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.springexamples.quickstart.sync;

import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.springexamples.common.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@SpringBootApplication
public class SampleApplication implements CommandLineRunner {

    private final Logger logger = LoggerFactory.getLogger(SampleApplication.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReactiveUserRepository reactiveUserRepository;

    public static void main(String[] args) {
        SpringApplication.run(SampleApplication.class, args);
    }

    public void run(String... var1) {

        final User testUser1 = new User("testId1", "testFirstName", "testLastName1");
        final User testUser2 = new User("testId2", "testFirstName", "testLastName2");

        logger.info("Using sync repository");

        // <Delete>

        userRepository.deleteAll();

        // </Delete>

        // <Create>

        logger.info("Saving user : {}", testUser1);
        userRepository.save(testUser1);

        // </Create>

        logger.info("Saving user : {}", testUser2);
        userRepository.save(testUser2);

        // <Read>        
        
        // This is a point read. See https://aka.ms/PointReadsInSpring for more information on the difference between point reads and queries.
        final User resultPointRead = userRepository.findById(testUser1.getId(), new PartitionKey(testUser1.getLastName())).get();
        logger.info("Found user (point read) : {}", resultPointRead);
        
        // </Read>        
        
        // <Query>
        // This is functionally the same as above, but is a query. Note that anything defined in userRepository would be a query. 
        // In order to do point reads in Cosmos DB using Spring, you need to explicitly use findById(String id, PartitionKey partitionKey) as above.
        final User resultQuery = userRepository.findByIdAndLastName(testUser1.getId(), testUser1.getLastName());
        logger.info("Found user (query): {}", resultQuery);

        
        Iterator<User> usersIterator = userRepository.findByFirstName("testFirstName").iterator();

        logger.info("Users by firstName : testFirstName");
        while (usersIterator.hasNext()) {
            logger.info("user is : {}", usersIterator.next());
        }

        // Get all records where last name is in a given array (equivalent to using IN)
        logger.info("Users by lastNames list...");
        ArrayList<String> lastNames = new ArrayList<String>();
        lastNames.add("testLastName1");
        lastNames.add("testLastName2");
        Iterator<User> usersIterator2 = userRepository.getUsersByLastNameList(lastNames).iterator();
        while (usersIterator2.hasNext()) {
            logger.info("user is : {}", usersIterator2.next());
        }

        logger.info("get all users...");
        // Get all records with simple select * from c
        Iterator<User> allUsersIterator = userRepository.getAllUsers().iterator();
        while (allUsersIterator.hasNext()) {
            logger.info("user is : {}", allUsersIterator.next());
        }
        
        logger.info("Using reactive repository");
        Flux<User> users = reactiveUserRepository.findByFirstName("testFirstName");
        users.map(u -> {
            logger.info("user is : {}", u);
            return u;
        }).subscribe();

        // </Query>
    }
}
