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
import java.util.concurrent.BlockingQueue;

/**
 * Periodically checks if busy connections are closed and if so, releases them into the pool.
 *
 * <p>
 * When the clients that already have connections from the pool, close their physical connections
 * without releasing it to the pool, this basically reduces the total number of pooled connections,
 * so when other clients request for a connection, they do not time out.
 * </p>
 *
 * @author andy.compeer@gmail.com
 */

public class ConnectionReleaser implements Runnable
{
    /**
     * Parent {@link ConnectionPoolManager} that instantiates this {@link java.lang.Runnable}
     * object.
     */
    private ConnectionPoolManager pool;

    /**
     * {@link PoolConfiguration} owned by the parent {@link ConnectionPoolManager}
     */
    private PoolConfiguration props;

    /**
     * {@link ConnectionPoolManager#busyConnections} owned by the parent {@link ConnectionPoolManager}
     */
    private BlockingQueue<Connection> busyConnections;

    /**
     * Interval time in milliseconds needed by {@link ConnectionReleaser} to check
     * for closed busy connections.
     * This value is set to {@link PoolConfiguration#getReleaserInterval()} value.
     * Can be modified to have more granularity or less
     * @example
     * <code>this.timeBetweenRuns = 3 * this.props.getReleaserInterval();<code>
     */
    private long timeBetweenRuns;

    /**
     * Unix epoch timestamp obtained by <code> System.currentTimeMillis() </code>
     */
    private long lastRun;

    /**
     * If this thread needs to be returned.
     */
    private boolean shouldClose = false;

    public static final Logger log = Logger.getLogger(ConnectionReleaser.class);

    /**
     * Constructor with {@link ConnectionPoolManager}
     */
    public ConnectionReleaser(ConnectionPoolManager pool) {
        this.pool = pool;
        this.props = this.pool.getProps();
    }

    /**
     * Busy connections are set by {@link ConnectionPoolManager}
     * when starting this thread.
     */
    public void setBusyConnections(BlockingQueue<Connection> busyConnections) {
        this.busyConnections = busyConnections;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        this.lastRun = System.currentTimeMillis();
        this.timeBetweenRuns = this.props.getReleaserInterval();
        log.debug("Time between runs: " + this.timeBetweenRuns);
        while (!this.shouldClose) {
            // pool is closed, join pool manager
            if (this.pool.isClosed()) {
               return;
            }
            if(((System.currentTimeMillis() - this.lastRun) > this.timeBetweenRuns)) {
                // make sure busy connections exist
                if (this.busyConnections != null && this.busyConnections.size() > 0) {
                    for (int i = 0; i < this.busyConnections.size(); i++) {
                        // just a peek
                        Connection conn = this.busyConnections.peek();
                        try {
                            // if connection is closed, release it
                            if (conn != null && conn.isClosed()) {
                                this.pool.releaseConnection(conn);
                                log.debug("Connection Released by ConnectionReleaser: " + conn);
                            }
                        } catch (SQLException e) {
                            log.error("Connection could not be released to the pool", e);
                        }
                    }//for
                    this.lastRun = System.currentTimeMillis();
                }
            }
        }//while
    }
}
