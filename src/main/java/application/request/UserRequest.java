package application.request;

import application.model.User;
import application.model.holder.Image;

public class UserRequest {
    public record RegisterRequest(
            String userId,
            String password,
            String nickname,
            String email) {
    }
    public record UpdateRequest(
        User user,
        String nickname,
        String oldPassword,
        String newPassword,
        String confirmPassword,
        Image image
    ){

    }
}
