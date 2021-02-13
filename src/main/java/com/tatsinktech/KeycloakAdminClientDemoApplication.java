package com.tatsinktech;

import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class KeycloakAdminClientDemoApplication {

   

    public static void main(String[] args) {
        SpringApplication.run(KeycloakAdminClientDemoApplication.class, args);
    }

    @Bean(name = "encryptorBean")
    public StringEncryptor stringEncryptor() {
        PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
        SimpleStringPBEConfig config = new SimpleStringPBEConfig();
        config.setPassword("AAAAB3NzaC1yc2EAAAABIwAAAQEA0Ke77Hs4NglBAC27ypDr+DPG7Kl5KI1emftgTH8JMn7ui0nakNxgWovJJnezGSNdGdQscFuLlG9yYUPlCZ9xTfSk/ce/lxpDIz7n/FSJu+z8JwGjORMaghcgdWznQ986yxMgZcNlB3eocgE4xD9mO0Zx5vHdX0oSUbTdmIPaGk6WS3zuRfyXooPrDbX6kQk6eTnNkRVOYkGb25Y69b7bGjMEEM/q4sD/w2l415tXfSfZhS9uDOexF7pSSxlqzpqzGWU5PJ4YENi10lSzKGOFCzrRENCrktA9H5+AobDEXpx17XK9B75D0bWJEV7GIgbDWw==");
        config.setAlgorithm("PBEWithMD5AndDES");
        config.setKeyObtentionIterations("1000");
        config.setPoolSize("1");
        config.setProviderName("SunJCE");
        config.setSaltGeneratorClassName("org.jasypt.salt.RandomSaltGenerator");
        config.setIvGeneratorClassName("org.jasypt.iv.NoIvGenerator");
        config.setStringOutputType("base64");
        encryptor.setConfig(config);

        return encryptor;
    }
}
