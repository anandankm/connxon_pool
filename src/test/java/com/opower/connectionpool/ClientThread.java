package com.opower.connectionpool;

import org.apache.log4j.Logger;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Random;

/**
 * TODO document
 * Runnable implementation to simulate multiple threads accessing 
 * {@link ConnectionPoolManager}
 */

public class ClientThread implements Runnable
{
    private ConnectionPoolManager pool;
    private PoolConfiguration props;
    private PoolHelper poolHelper;
    private Random random;
    public static final Logger log = Logger.getLogger(ClientThread.class);

    public ClientThread(ConnectionPoolManager pool, Random random) {
        this.pool = pool;
        this.props = this.pool.getProps();
        this.poolHelper = new PoolHelper(this.pool);
        this.random = random;
    }

    public void run() {
        log.info("Client started");
        Connection conn = this.poolHelper.getConnxFromPool();
        try {
            int rSleep = this.randomRange(100,1000);
            log.info("Sleeping for: " + rSleep);
            Thread.sleep(rSleep);
            this.poolHelper.sqlTest(conn, SetupHelper.testQuery, SetupHelper.checkRowValues, true);
        } catch (SQLException e) {
            log.error("Testing sql query failed", e);
        } catch (InterruptedException e) {
            log.error("Interrupted", e);
        } finally {
            this.poolHelper.closeConnxon(conn);
            log.info("Client finished");
        }
    }

    private int randomRange(int s, int e) {
        if (s >= e) {
            return 100;
        }
        long range = (long) e - (long) s + 1;
        range = (long) (range * this.random.nextDouble());
        return (int) (range + s);
    }
}
