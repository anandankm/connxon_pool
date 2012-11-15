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

import org.apache.log4j.Logger;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Random;

/**
 * {link java.lang.Runnable} implementation to simulate multiple clients accessing
 * {@link ConnectionPoolManager}
 */

public class ClientThread implements Runnable
{
    /**
     * {@link ConnectionPoolManager} for this client to access
     */
    private ConnectionPoolManager pool;
    /**
     * {@link PoolConfiguration} specified for this pool
     */
    private PoolConfiguration props;
    /**
     * To access pool functions, sql functions and log fails, if any.
     */
    private PoolHelper poolHelper;
    /**
     * A {java.util.Random} pseudo-random-number-generator to access the pool
     * in a random fashion
     */
    private Random random;

    /**
     * Logger
     */
    public static final Logger log = Logger.getLogger(ClientThread.class);

    public ClientThread(ConnectionPoolManager pool, Random random) {
        this.pool = pool;
        this.props = this.pool.getProps();
        this.poolHelper = new PoolHelper(this.pool);
        this.random = random;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        log.info("Client started");
        Connection conn = this.poolHelper.getConnxFromPool();
        try {
            int rSleep = this.randomRange(100,1000);
            Thread.sleep(rSleep);
            this.poolHelper.sqlTest(conn, SetupHelper.testQuery, SetupHelper.checkRowValues);
        } catch (SQLException e) {
            log.error("Testing sql query failed", e);
        } catch (InterruptedException e) {
            log.error("Interrupted", e);
        } finally {
            this.poolHelper.closeConnxon(conn);
            log.info("Client finished");
        }
    }

    /**
     * Get a random number in the range of [s, e]
     */
    private int randomRange(int s, int e) {
        if (s >= e) {
            return 100;
        }
        long range = (long) e - (long) s + 1;
        range = (long) (range * this.random.nextDouble());
        return (int) (range + s);
    }
}
