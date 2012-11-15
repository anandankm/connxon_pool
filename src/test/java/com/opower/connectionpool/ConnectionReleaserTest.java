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
public class ConnectionReleaserTest
{
    private ConnectionPoolManager poolManager;
    private PoolConfiguration poolProps;
    private PoolHelper poolHelper;
    public static final Logger log = Logger.getLogger(ConnectionReleaserTest.class);

    @BeforeClass
    public static void testSetup() throws SQLException, IOException {
        log.info("-----------------------------------");
        log.info("-       POOL RELEASER TEST        -");
        log.info("-----------------------------------");
        SetupHelper.getProperties();
        SetupHelper.dbSetup();
    }

    @Before
    public void setup() {
        log.info("Setting up ConnectionPoolManager");
        try {
            this.poolManager = new ConnectionPoolManager(SetupHelper.setupProperties,
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

    /**
     * Essentially test {@link ConnectionReleaser}
     */
    @Test
    public void testMultipleClients() {
        // three times the maximum number of connections.
        int numClients = 3 * this.poolProps.getMaxConnections();
        log.info("Starting multiple clients test. Number of clients: " + numClients);
        LinkedList<Thread> clients = new LinkedList<Thread>();
        Random random = new Random();
        for (int i = 0; i < numClients; i++) {
            ClientThread client = new ClientThread(this.poolManager, random);
            Thread t = new Thread(client);
            t.setName("Client-" + i);
            t.start();
            clients.add(t);
        }
        while (clients.size() > 0) {
            Thread runningClient = clients.getFirst();
            boolean clientDead = false;
            if (runningClient != null && runningClient.isAlive()) {
                try {
                    runningClient.join(500);
                } catch (InterruptedException e) {
                    log.error("Client thread Interrupted", e);
                }
                if (!runningClient.isAlive()) {
                    clientDead = true;
                }
            } else {
                clientDead = true;
            }
            if (clientDead) {
                clients.removeFirst();
                assertTrue(this.poolManager.getSize() == this.poolManager.getAvailableSize() + this.poolManager.getBusySize());
                assertTrue(this.poolManager.getSize() <= this.poolProps.getMaxConnections());
                assertTrue(this.poolManager.getAvailableSize() + this.poolManager.getBusySize() <= this.poolProps.getMaxConnections());
            }
        }
        log.info(this.poolManager.capacityInfo("Final count.", "\n"));
        log.info("Finished multiple clients test");
    }
}
