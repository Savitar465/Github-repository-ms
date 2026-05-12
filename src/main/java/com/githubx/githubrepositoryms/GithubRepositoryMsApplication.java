package com.githubx.githubrepositoryms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(
    scanBasePackages = "com.githubx.githubrepositoryms"
)
public class GithubRepositoryMsApplication {

    public static void main(String[] args) {
        SpringApplication.run(GithubRepositoryMsApplication.class, args);
    }

}
