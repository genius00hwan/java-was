package application.auth;

public interface AuthLoader <T extends AuthTarget >{
    T load(String id);
}
