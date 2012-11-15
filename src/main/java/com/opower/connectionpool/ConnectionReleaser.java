package com.opower.connectionpool;

import org.apache.log4j.Logger;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.BlockingQueue;

/**
 * TODO document
 * Periodically checks if busy connections are closed and if so, releases them into the pool.
 *
 * When the clients that already have connections from the pool, close their physical connections
 * without releasing it to the pool, this basically reduces the total number of pooled connections,
 * so when other clients request for a connection, they do not time out.
 */

public class ConnectionReleaser implements Runnable
{
    private ConnectionPoolManager pool;
    private PoolConfiguration props;
    private BlockingQueue<Connection> busyConnections;

    private long timeBetweenRuns;
    private long lastRun;
    private boolean shouldClose = false;

    public static final Logger log = Logger.getLogger(ConnectionReleaser.class);

    public ConnectionReleaser(ConnectionPoolManager pool) {
        this.pool = pool;
        this.props = this.pool.getProps();
    }

    public void setBusyConnections(BlockingQueue<Connection> busyConnections) {
        this.busyConnections = busyConnections;
    }

    public void run() {
        this.lastRun = System.currentTimeMillis();
        this.timeBetweenRuns = this.props.getReleaserInterval();
        log.info("Time between runs: " + this.timeBetweenRuns);
        while (!this.shouldClose) {
            if (this.pool.isClosed()) {
               return;
            }
            if(((System.currentTimeMillis() - this.lastRun) > this.timeBetweenRuns)) {
                if (this.busyConnections != null && this.busyConnections.size() > 0) {
                    for (int i = 0; i < this.busyConnections.size(); i++) {
                        Connection conn = this.busyConnections.peek();
                        try {
                            if (conn != null && conn.isClosed()) {
                                this.pool.releaseConnection(conn);
                                log.debug("Connection Released by ConnectionReleaser: " + conn);
                            }
                        } catch (SQLException e) {
                            log.error("Connection could not be released to the pool", e);
                        }
                    }
                    this.lastRun = System.currentTimeMillis();
                }
            }
        }//while
    }
}
