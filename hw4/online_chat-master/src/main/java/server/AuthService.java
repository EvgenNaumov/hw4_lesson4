package server;

import database.ConnectDB;

import java.sql.SQLException;

public interface AuthService {
    void start();
    void stop();
    String getNickByLoginAndPass(String login, String password);
}
