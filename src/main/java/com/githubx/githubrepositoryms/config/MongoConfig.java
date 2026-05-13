package com.githubx.githubrepositoryms.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Configuration
public class MongoConfig extends AbstractMongoClientConfiguration {

    @Value("${MONGO_HOST:localhost}")
    private String host;

    @Value("${MONGO_PORT:27017}")
    private int port;

    @Value("${MONGO_DATABASE:github_repository_ms}")
    private String database;

    @Value("${MONGO_USERNAME:}")
    private String username;

    @Value("${MONGO_PASSWORD:}")
    private String password;

    @Value("${MONGO_AUTH_DATABASE:admin}")
    private String authDatabase;

    @Override
    protected String getDatabaseName() {
        return database;
    }

    @Override
    public MongoClient mongoClient() {
        String uri;
        if (username != null && !username.isEmpty()) {
            String encodedUser = URLEncoder.encode(username, StandardCharsets.UTF_8);
            String encodedPass = URLEncoder.encode(password, StandardCharsets.UTF_8);
            uri = String.format("mongodb://%s:%s@%s:%d/%s?authSource=%s",
                    encodedUser, encodedPass, host, port, database, authDatabase);
        } else {
            uri = String.format("mongodb://%s:%d/%s", host, port, database);
        }

        System.out.println("Connecting to MongoDB with URI: " + uri.replaceAll(":.*@", ":****@"));

        ConnectionString connectionString = new ConnectionString(uri);
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .build();

        return MongoClients.create(settings);
    }

    @Bean
    public MongoTemplate mongoTemplate() {
        return new MongoTemplate(mongoClient(), getDatabaseName());
    }
}
