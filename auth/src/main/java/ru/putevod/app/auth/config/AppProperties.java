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

    @Data
    public static class Jwt {
        private String secret;
        private long accessTokenExpirationMs;
        private long refreshTokenExpirationMs;
        private long anonymousTokenExpirationMs;
    }

    @Data
    public static class Email {
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