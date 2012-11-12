package com.opower.connectionpool;

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

/**
 * Unit test for ConnectionPoolManager.
 */
public class ConnectionPoolManagerTest 
{
    public ConnectionPoolManager poolManager;

    @Before
    public void setup() {
        String url = "jdbc:mysql://localhost:3306/opower_connxn?allowMultiQueries=true";
        String user = "root";
        String pass = "pass";
        try {
            Class.forName(PoolProperties.DEFAULT_DRIVERNAME);
        } catch (ClassNotFoundException e) {
            fail("Class not found: " + PoolProperties.DEFAULT_DRIVERNAME + "." +
                    "Make sure you have the jar file in the classpath. Exception: " + e.getMessage());
        }
        try {
            Connection conn = DriverManager.getConnection(url, user, pass);
            String sql = "DROP TABLE IF EXISTS opower_test;"+
                "CREATE TABLE opower_test(" +
                " Userid INT(11) UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY," +
                " Name VARCHAR(64) NOT NULL DEFAULT '0'" +
                ")ENGINE=InnoDB AUTO_INCREMENT=18745490 DEFAULT CHARSET=utf8;" +
                "INSERT INTO opower_test VALUES (1, 'Anandan'), (2, 'Opower');";
            conn.createStatement().execute(sql);
            poolManager = new ConnectionPoolManager(url, user, pass);
        } catch (SQLException e) {
            fail("Failed to setup database and connection pool. Exception: " + e.getMessage());
        }
        System.out.println("Setup complete");
    }

    @Test
    public void  getConnectionTest() {
        System.out.println("First Test");
        PoolProperties props = this.poolManager.getProps();
        Connection conn = null;
        try {
            conn = this.poolManager.getConnection();
        } catch (SQLException e) {
            fail("Failed to get connection from pool. Exception: " + e.getMessage());
        }
        ResultSet res = null;
        String simpleQuery = "SELECT * FROM opower_test";
        try {
            res = conn.createStatement().executeQuery(simpleQuery);
            assertTrue(res.next());
            assertEquals(1, res.getInt(1));
            assertEquals("Anandan", res.getString(2));
        } catch (SQLException e)  {
            fail("Failed @getConnectionTest to execute query: " + simpleQuery + ". Exception: " + e.getMessage());
        }
    }

    @Test
    public void  test2() {
        System.out.println("Second Test");
    }
}
