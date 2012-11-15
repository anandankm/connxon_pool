package com.opower.connectionpool;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import org.apache.log4j.Logger;

/**
 *  Provides helper functions to {@Test} methods in {@link ConnectionPoolManagerTest}
 */
public class PoolHelper 
{
    public static final Logger log = Logger.getLogger(PoolHelper.class);
    private ConnectionPoolManager pool;
    private boolean fail = true;

    public void setFail(boolean fail) {
        this.fail = fail;
    }

    public PoolHelper(ConnectionPoolManager pool) {
        this.pool = pool;
    }

    public Connection getConnxFromPool() {
        Connection conn = null;
        try {
            conn = this.pool.getConnection();
        } catch (SQLException e) {
            log.error(e);
            if (this.fail) {
                fail("Failed to get connection from pool. Exception: " + e.getMessage());
            }
        }
        return conn;
    }

    public void releaseConnxToPool(Connection conn) {
        try {
            this.pool.releaseConnection(conn);
        } catch (SQLException e) {
            log.error(e);
            if (this.fail) {
                fail("Failed to release connection. Exception: " + e.getMessage());
            }
        }
    }

    public void closePool() {
        try {
            this.pool.close();
        } catch (SQLException e) {
            log.error(e);
            if (this.fail) {
                fail("Failed to close the connection pool. Exception: " + e.getMessage());
            }
        }
    }

    public void closeConnxon(Connection conn) {
        try {
            conn.close();
        } catch (SQLException e) {
            log.error(e);
            if (this.fail) {
                fail("Failed to close connection. Exception: " + e.getMessage());
            }
        }
    }

    public void sqlTest(Connection conn, String sql, boolean checkRowValues) throws SQLException {
        if (sql.length() == 0) {
            fail("Provide a test query property: TEST_QUERY in setup.properties file");
        }
        ResultSet res = null;
        try {
            res = conn.createStatement().executeQuery(sql);
            if (checkRowValues) {
                this.checkRowValues(res);
            }
            res.close();
        } catch (SQLException e)  {
            log.error(e);
            if (this.fail) {
                fail("Failed to execute query: " + sql + ". Exception: " + e.getMessage());
            } else {
                throw e;
            }
        }
    }

    protected void checkRowValues(ResultSet res) throws SQLException {
        assertTrue(res.next());
        assertEquals(1, res.getInt(1));
        assertEquals("Anandan", res.getString(2));
        assertTrue(res.next());
        assertEquals(2, res.getInt(1));
        assertEquals("Opower", res.getString(2));
    }

}
