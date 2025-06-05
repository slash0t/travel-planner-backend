package ru.putevod.app.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private Jwt jwt = new Jwt();
    private Email email = new Email();
    private Security security = new Security();
    private Services services = new Services();
    private String frontendUrl;
    private int verificationTokenExpirationHours = 24;
    private int resetTokenExpirationMinutes = 15;
    private String authToken;

    @Data
    public static class Jwt {
        private String secret;
        private long accessTokenExpirationMs;
        private long refreshTokenExpirationMs;
        private long anonymousTokenExpirationMs = 1800000;
    }

    @Data
    public static class Services {
        private String plannerUrl;
    }

    @Data
    public static class Email {
        private String from;
        private Verification verification = new Verification();
        private PasswordReset passwordReset = new PasswordReset();

        @Data
        public static class Verification {
            private long expiration;
        }

        @Data
        public static class PasswordReset {
            private long expiration;
        }
    }

    @Data
    public static class Security {
        private String allowedOrigins;
        private String allowedMethods;
        private String allowedHeaders;
    }
} 