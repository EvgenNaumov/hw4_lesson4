package database;

import java.sql.*;

public class ConnectDB {
    private Connection conn;
    private ResultSet resultSet;
    private Statement statement;

    public ConnectDB() throws SQLException {
        try{
            conn = null;
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:geek_chat.db3");
        }catch (ClassNotFoundException | SQLException e){
            e.printStackTrace();
            throw new SQLException("ошибка подключения к базе данных");
        }

    }
    public void closeConn(){
        try{
            conn.close();
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    public String getNickbyLoginAndPass(String login, String pass)  {
        String textQuery =  "SELECT nick FROM users WHERE nick = ? and password = ?";
        try {
        PreparedStatement ps = conn.prepareStatement(textQuery);
        ps.setString(1, login);
        ps.setString(2,pass);
            resultSet = ps.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString(1);
            } else {
                return null;
            }
        }catch (SQLException e){
            return null;
        }
    }
}
