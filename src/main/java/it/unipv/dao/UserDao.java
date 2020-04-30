package it.unipv.dao;

import it.unipv.model.User;

import java.util.List;

public interface UserDao {
    List<User> retrieveUserList();
    void insertNewUser(User toInsert);
    void deleteUser(User toDelete);
    void updateUser(User toUpdate);
}
