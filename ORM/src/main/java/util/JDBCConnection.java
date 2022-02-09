package util;

//Imports from java.sql -> JDBC
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class JDBCConnection {

    /*
    We are going create a similar idea to what is called a Singleton.
    Singleton -> Design Pattern in which you only ever want One instance
    of the Object to ever exist.

    - Prevent additional Object creations by privatizing our constructor
    and creating a public method that controls when if at all a new Object is created.
     */

    private static Connection conn = null;

    public static Connection getConnection() {



        /*
        To establish a new connection if one doesn't exist,
        otherwise return the current connection.

        Credentials: url (endpoint), username, password
         */

        if (conn == null) {
            //Establish a new Connection

            Properties props = new Properties();
            try {

                Class.forName("org.postgresql.Driver");

                props.load(JDBCConnection.class.getClassLoader().getResourceAsStream("connection.properties"));
//                props.load(new FileInputStream(JDBCConnection.class.getClassLoader().getResource("connection.properties").getFile()));
//                props.load(new FileReader("src/main/resources/connection.properties"));

                String endpoint = props.getProperty("endpoint");
                //URL Format (Postgresql JDBC)
                //jdbc:postgresql://[endpoint]/[database]
                String url = "jdbc:postgresql://" + endpoint + "/postgres";
                String username = props.getProperty("username");
                String password = props.getProperty("password");

                conn = DriverManager.getConnection(url, username, password);

            } catch (IOException | SQLException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        return conn;
    }


    /*
    THIS IS FOR TESTING PURPOSES ONLY!!!!!!!!!!!!!!
    NOT NEEDED TO ACTUALLY USE JDBC
     */
    public static void main(String[] args) {

        Connection conn1 = getConnection();
        Connection conn2 = getConnection();

        System.out.println(conn1);
        System.out.println(conn2);

    }
}