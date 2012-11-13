package com.opower.connectionpool;

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
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
    protected ConnectionPoolManager poolManager;
    public static final String TEST_DB = "opower_connxn";
    public static final String TEST_TABLE = "opower_test";
    protected String testMysqlUser = "root";
    protected String testMysqlPass = "pass";

    @Before
    public void setup() {
        String url = "jdbc:mysql://localhost:3306?allowMultiQueries=true";
        try {
            Class.forName(PoolProperties.DEFAULT_DRIVERNAME);
        } catch (ClassNotFoundException e) {
            fail("Class not found: " + PoolProperties.DEFAULT_DRIVERNAME + "." +
                    "Make sure you have the jar file in the classpath. Exception: " + e.getMessage());
        }
        try {
            this.dbSetup(url);
            url = "jdbc:mysql://localhost:3306/" + TEST_DB + "?allowMultiQueries=true";
            this.poolManager = new ConnectionPoolManager(url, testMysqlUser, testMysqlPass);
        } catch (SQLException e) {
            fail("Failed to setup database and connection pool. Exception: " + e.getMessage());
        }
        System.out.println("Setup complete");
    }

    protected void dbSetup(String url) throws SQLException {
        Connection conn = DriverManager.getConnection(url, testMysqlUser, testMysqlPass);
        String dbSetupSql = "DROP DATABASE IF EXISTS " + TEST_DB + "; " + 

            "CREATE DATABASE " + TEST_DB + "; " + 

            "USE  " + TEST_DB + "; " +

            "DROP TABLE IF EXISTS " + TEST_TABLE + "; " +

            "CREATE TABLE " + TEST_TABLE + "(" +
            " Userid INT(11) UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY," +
            " Name VARCHAR(64) NOT NULL DEFAULT '0'" +
            ")ENGINE=InnoDB DEFAULT CHARSET=utf8; " +

            "INSERT INTO " + TEST_TABLE + " VALUES (1, 'Anandan'), (2, 'Opower');";
        conn.createStatement().execute(dbSetupSql);
    }

    @Test
    public void getConnectionTest() {
        Connection conn = this.getConnxFromPool();
        this.sqlTest(conn);
        this.closeConnxon(conn);
    }

    @Test
    public void propertiesTest() {
        Connection conn = this.getConnxFromPool();
        assertTrue(this.poolManager.containsConnection(conn));
        PoolProperties props = this.poolManager.getProps();
        assertEquals(1, this.poolManager.getNumUsedConnections());
        assertEquals(props.initialSize - 1, this.poolManager.getNumAvailConnections());
        this.closeConnxon(conn);
    }

    @Test (expected=SQLException.class)
    public void maxConnectionsTest() throws SQLException {
        PoolProperties props = this.poolManager.getProps();
        for (int i = 0; i < props.maxConnections; i++) {
            Connection conn = this.getConnxFromPool();
            this.closeConnxon(conn);
        }
        Connection conn = this.poolManager.getConnection();
        // Not reachable, since previous statement throws SQLException
        this.closeConnxon(conn);
    }

    @Test
    public void releaseToPoolTest() {
        PoolProperties props = this.poolManager.getProps();
        Connection conn = this.getConnxFromPool();
        assertEquals(1, this.poolManager.getNumUsedConnections());
        assertEquals(props.initialSize - 1, this.poolManager.getNumAvailConnections());
        try {
            this.poolManager.releaseConnection(conn);
        } catch (SQLException e) {
            fail("Failed to release connection. Exception: " + e.getMessage());
        }
        assertEquals(0, this.poolManager.getNumUsedConnections());
        assertEquals(props.initialSize, this.poolManager.getNumAvailConnections());
        this.closeConnxon(conn);
    }

    protected void sqlTest(Connection conn) {
        String sql = "SELECT * FROM opower_test";
        ResultSet res = null;
        try {
            res = conn.createStatement().executeQuery(sql);
            assertTrue(res.next());
            assertEquals(1, res.getInt(1));
            assertEquals("Anandan", res.getString(2));
            assertTrue(res.next());
            assertEquals(2, res.getInt(1));
            assertEquals("Opower", res.getString(2));
            res.close();
        } catch (SQLException e)  {
            fail("Failed to execute query: " + sql + ". Exception: " + e.getMessage());
        }
    }

    protected Connection getConnxFromPool() {
        Connection conn = null;
        try {
            conn = this.poolManager.getConnection();
        } catch (SQLException e) {
            fail("Failed to get connection from pool. Exception: " + e.getMessage());
        }
        return conn;
    }

    protected void closeConnxon(Connection conn) {
        try {
            conn.close();
        } catch (SQLException e) {
            fail("Failed to close connection. Exception: " + e.getMessage());
        }
    }
}
