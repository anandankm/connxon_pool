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
package com.grooveshark.connxonpool;

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import org.junit.BeforeClass;

import java.util.Random;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Enumeration;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.FileNotFoundException;
import java.io.IOException;

import static org.junit.Assert.fail;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import org.apache.log4j.Logger;

/**
 * Unit tests for ConnectionPoolManager.
 */
public class ConnectionPoolManagerTest
{
    private ConnectionPoolManager poolManager;
    private PoolConfiguration poolProps;
    private PoolHelper poolHelper;
    public static final Logger log = Logger.getLogger(ConnectionPoolManagerTest.class);

    @BeforeClass
    public static void testSetup() throws SQLException, IOException {
        log.info("--------------------------");
        log.info("-  CONNECTION POOL TEST  -");
        log.info("--------------------------");
        // get test properties from setup.properties file
        SetupHelper.getProperties();
        SetupHelper.dbSetup();
    }

    @Before
    public void setup() {
        log.info("Setting up ConnectionPoolManager");
        try {
            PoolConfiguration props = new PoolProperties(SetupHelper.setupProperties);
            //props.setRunReleaser(false);
            this.poolManager = new ConnectionPoolManager(props,
                                                         SetupHelper.testURL,
                                                         SetupHelper.testUser,
                                                         SetupHelper.testPass);
            this.poolProps = this.poolManager.getProps();
            this.poolHelper = new PoolHelper(this.poolManager);
        } catch (SQLException e) {
            log.error("Failed to setup connection pool." +
                      "; TEST_USER: " + SetupHelper.testUser +
                      "; TEST_PASSWORD: " + SetupHelper.testPass +
                      "; TEST_URL " + SetupHelper.testURL, e);
            fail("Failed to setup connection pool. Exception: " + e.getMessage());
        }
    }

    @After
    public void teardown() {
        if (!this.poolManager.isClosed()) {
            try {
                this.poolManager.close();
            } catch (SQLException e) {
                fail("Failed to close the connection pool. Exception: " + e.getMessage());
            }
        }
    }

    @Test
    public void getConnectionTest() {
        log.info("Starting getConnectionTest()");
        Connection conn = this.poolHelper.getConnxFromPool();
        try {
            this.poolHelper.sqlTest(conn, SetupHelper.testQuery, SetupHelper.checkRowValues);
        } catch (SQLException e) {
            // logging happens in PoolHelper.
        }
        this.poolHelper.closeConnxon(conn);
        log.info("Finished getConnectionTest()");
    }

    @Test
    public void propertiesTest() {
        log.info("Starting propertiesTest()");
        Connection conn = this.poolHelper.getConnxFromPool();
        assertTrue(this.poolManager.containsConnection(conn));
        assertEquals(1, this.poolManager.getBusySize());
        assertEquals(this.poolProps.getInitialSize() - 1, this.poolManager.getAvailableSize());
        this.poolHelper.closeConnxon(conn);
        log.info("Finished propertiesTest()");
    }

    @Test
    public void maxConnectionsTest() throws SQLException {
        log.info("Starting maxConnectionsTest()");
        for (int i = 0; i < this.poolProps.getMaxConnections(); i++) {
            Connection conn = this.poolHelper.getConnxFromPool();
            this.poolHelper.closeConnxon(conn);
        }
        Connection conn;
        try {
            conn = this.poolManager.getConnection();
        } catch (SQLException e) {
            // if runReleaser is false
            assertTrue(e.getMessage().contains("Timed out. No available connection"));
            log.debug(this.poolManager.capacityInfo("Checking", "\n"));
            log.info("Finished maxConnectionsTest()");
            return;
        }
        try {
            this.poolHelper.sqlTest(conn, SetupHelper.testQuery, SetupHelper.checkRowValues);
        } catch (SQLException e) {
            throw e;
            // logging happens in PoolHelper.
        }
        this.poolHelper.closeConnxon(conn);
        log.info("Finished maxConnectionsTest()");
    }

    @Test (expected=SQLException.class)
    public void releaseToPoolTest() throws SQLException {
        log.info("Starting releaseToPoolTest()");
        Connection conn = this.poolHelper.getConnxFromPool();
        assertEquals(1, this.poolManager.getBusySize());
        assertEquals(this.poolProps.getInitialSize() - 1, this.poolManager.getAvailableSize());
        this.poolHelper.releaseConnxToPool(conn);
        assertEquals(0, this.poolManager.getBusySize());
        assertEquals(this.poolProps.getInitialSize(), this.poolManager.getAvailableSize());
        this.poolHelper.closeConnxon(conn);
        // Create a foreign connection
        conn=this.createAForiegnConnection();
        // this should log (in debug mode) "Does not belong to the pool"
        // and close the connection
        this.poolHelper.releaseConnxToPool(conn);
        this.poolHelper.setFail(false);
        try {
            this.poolHelper.sqlTest(conn, SetupHelper.testQuery, SetupHelper.checkRowValues);
        } catch (SQLException e) {
            assertTrue(e.getMessage().contains("No operations allowed after connection closed"));
            log.info("Finished releaseToPoolTest()");
            throw e;
        }
    }

    @Test (expected=SQLException.class)
    public void closeTest() throws SQLException {
        log.info("Starting closeTest()");
        Connection conn = this.poolHelper.getConnxFromPool();
        this.poolHelper.sqlTest(conn, SetupHelper.testQuery, SetupHelper.checkRowValues);
        this.poolHelper.closeConnxon(conn);
        this.poolHelper.closePool();
        try {
            conn = this.poolManager.getConnection();
        } catch (SQLException e) {
            assertTrue(e.getMessage().contains("Connection pool is closed"));
            log.debug(this.poolManager.capacityInfo(e.getMessage(), "\n"));
            throw e;
        }
    }

    private Connection createAForiegnConnection() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(SetupHelper.testURL,
                                               SetupHelper.testUser,
                                               SetupHelper.testPass);
        } catch (SQLException e) {
            log.error(e);
            fail("Failed to get a foreign connection. Exception: " + e.getMessage());
        }
        return conn;
    }
}
