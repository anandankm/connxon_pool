/**
 *                  GNU GENERAL PUBLIC LICENSE
 *
 *  Copyright (C) 2012 Anandan.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.opower.connectionpool;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import org.apache.log4j.Logger;

/**
 *  Provides helper functions to {@link org.junit.Test} methods in {@link ConnectionPoolManagerTest}
 */
public class PoolHelper
{
    public static final Logger log = Logger.getLogger(PoolHelper.class);
    private ConnectionPoolManager pool;
    /**
     * Whether to fail on any exception.
     * Some of them are expected to be thrown in the test methods.
     */
    private boolean fail = true;

    public void setFail(boolean fail) {
        this.fail = fail;
    }

    public PoolHelper(ConnectionPoolManager pool) {
        this.pool = pool;
    }

    /**
     * Gets a connection from the pool
     */
    public Connection getConnxFromPool() {
        Connection conn = null;
        try {
            conn = this.pool.getConnection();
        } catch (SQLException e) {
            log.debug(e);
            if (this.fail) {
                fail("Failed to get connection from pool. Exception: " + e.getMessage());
            }
        }
        return conn;
    }

    /**
     * Releases a connection to the pool
     */
    public void releaseConnxToPool(Connection conn) {
        try {
            this.pool.releaseConnection(conn);
        } catch (SQLException e) {
            log.debug(e);
            if (this.fail) {
                fail("Failed to release connection. Exception: " + e.getMessage());
            }
        }
    }

    /**
     * Closes a {@link ConnectionPoolManager}
     */
    public void closePool() {
        try {
            this.pool.close();
        } catch (SQLException e) {
            log.debug(e);
            if (this.fail) {
                fail("Failed to close the connection pool. Exception: " + e.getMessage());
            }
        }
    }

    /**
     * Closes a {@link java.sql.Connection}
     */
    public void closeConnxon(Connection conn) {
        try {
            conn.close();
        } catch (SQLException e) {
            log.debug(e);
            if (this.fail) {
                fail("Failed to close connection. Exception: " + e.getMessage());
            }
        }
    }

    /**
     * Runs a sql query test on the connection.
     * @param checkRowValues set this true only if {@link #checkRowValues(ResultSet res)}
     *        needs to be executed and the result set contains exactly the following rows.
     *        <code>
     *        {1, 'Anandan'}
     *        {2, 'Opower'}
     *        </code>
     */
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
            log.debug(e);
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
