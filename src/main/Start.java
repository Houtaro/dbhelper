package main;

import java.sql.SQLException;

public class Start {
    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        DBHelper user = new DBHelper("user", 5);
        user.connect();
        user.set("firstname", "Darrell");
        user.where("lastname", "Arabis").where("age", 25);
        user.save();
        
    }
}
