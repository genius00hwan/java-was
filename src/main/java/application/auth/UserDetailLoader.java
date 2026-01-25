package application.auth;

import annotation.Singleton;

import application.db.UserDao;
import application.model.User;

@Singleton
public class UserDetailLoader implements AuthLoader<User> {
    private final UserDao userDao;

    public UserDetailLoader(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public User load(String userId){
        return userDao.findUserById(userId);
    }
}
