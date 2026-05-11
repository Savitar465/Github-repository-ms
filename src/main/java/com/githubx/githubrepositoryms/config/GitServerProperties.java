package com.githubx.githubrepositoryms.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "git")
@Data
public class GitServerProperties {

    private Server server = new Server();

    @Data
    public static class Server {
        private String httpUrl = "http://192.168.100.150:9080";
        private String sshHost = "192.168.100.150";
        private int sshPort = 2222;
        private String reposPath = "/git-server/repos";
    }
}
