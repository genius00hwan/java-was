package application.auth;

public abstract class AuthTarget {
    protected final String userId;
    protected String password;

    public AuthTarget(String userId, String password) {
        this.userId = userId;
        this.password = encrypt(password);
    }

    protected abstract String encrypt(String rawPassword);
    public abstract boolean isValidPassword(String password);

    public String userId() {
        return userId;
    }

    public String password() {
        return password;
    }
}
