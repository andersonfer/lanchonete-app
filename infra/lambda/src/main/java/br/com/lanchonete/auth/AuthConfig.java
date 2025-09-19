package br.com.lanchonete.auth;

public class AuthConfig {
    private final String userPoolId;
    private final String clientId;
    private final String autoatendimentoUrl;

    public AuthConfig(String userPoolId, String clientId, String autoatendimentoUrl) {
        this.userPoolId = userPoolId;
        this.clientId = clientId;
        this.autoatendimentoUrl = autoatendimentoUrl;
    }

    public static AuthConfig fromEnvironment() {
        return new AuthConfig(
            System.getenv("USER_POOL_ID"),
            System.getenv("CLIENT_ID"),
            System.getenv("AUTOATENDIMENTO_URL")
        );
    }

    public String getUserPoolId() {
        return userPoolId;
    }

    public String getClientId() {
        return clientId;
    }

    public String getAutoatendimentoUrl() {
        return autoatendimentoUrl;
    }
}