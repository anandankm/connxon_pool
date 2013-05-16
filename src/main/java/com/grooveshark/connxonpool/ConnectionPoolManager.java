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

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.DriverManager;
import java.sql.Driver;
import java.util.Properties;
import java.util.LinkedList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

/**
 * Connection pool manager. An implementation of {@link ConnectionPool}
 * <br/>
 * Manages all pool connection activities. When instantiating, it requires an
 * instance of {@link PoolConfiguration} or an instance of {@link java.util.Properties}
 * with all the pool related properties, a username and password to make a {@link java.sql.Connection}
 * <p>
 * When a connection is not available (if the {@link #size} of this connection pool is equal to
 * {@link PoolProperties#maxConnections}, then a connection cannot be obtained), a client is blocked
 * for {@link PoolProperties#maxWait} milliseconds and after which an {@link java.sql.SQLException} is thrown.
 * {@link #releaseConnection()} releases a connection only if it belongs to the pool, otherwise closes it.
 * </p>
 *
 * A {@link ConnectionReleaser} is used in order to make sure connections, closed by clients or
 * through some failures, are released/removed from {@link #busyConnections}, so the {@link #size} is not maxed out.
 *
 * @author andy.compeer@gmail.com
 * @see java.sql.DriverManager#getConnection(java.lang.String, java.lang.String, java.lang.String)
 * @see java.sql.Driver#connect(java.lang.String, java.util.Properties)
 *
 */
public class ConnectionPoolManager implements ConnectionPool {

    /**
     * URL with [host] [port] [database]
     * @example
     *    <code>String url = "jdbc:mysql://localhost:3306/beluga?autoReconnect=true"</code>
     */
    private String url;
    /**
     * Username to connect to the database
     */
    private String user;
    /**
     * Password to connect to the database
     */
    private String pass;

    /**
     * {@link java.sql.Driver} used by this pool to make a reconnection
     * and get a {@link java.sql.Connection}
     */
    private Driver driver;

    /**
     * Logger
     */
    public static final Logger log = Logger.getLogger(ConnectionPoolManager.class);

    /**
     * Size of the pool at any given time.
     * Incremented only at two places. One at {@link #createAndAdd}, when we create new connection
     * if no connection is available and another at {@link #initializePool}, when a new
     * {@link ConnectionPoolManager} is instantiated.
     */
    private AtomicInteger size = new AtomicInteger(0);

    /**
     * Atomic Flag to see if pool is closed
     */
    private AtomicBoolean closed = new AtomicBoolean(false);

    /**
     * Thread safe list of connections currently used by the clients
     */
    private BlockingQueue<Connection> busyConnections;

    /**
     * Thread safe list of connections available to the clients
     */
    private BlockingQueue<Connection> availableConnections;

    /**
     * A {@link Runnable} implementation that releases connections
     * periodically. The time period between which it checks {@link #busyConnections}
     * for closed connections is specified by {@link PoolConfiguration#setReleaserInterval()}
     */
    private ConnectionReleaser releaser;

    /**
     * Thread allocated with a {@link ConnectionReleaser} instance.
     * Useful when closing the {@link ConnectionPoolManager} using {@link #close()} method
     * to join it back and to make sure the thread is not alive and running as a daemon
     */
    private Thread releaserThread;

    /**
     * Pool properties for this instance {@link ConnectionPoolManager}
     */
    private PoolConfiguration props;

    /**
     * Constructor with a given {@link PoolConfiguration}
     *
     *
     * @param props - {@link PoolConfiguration} defining pool properties
     * @param url - url String used to make a {@link a java.sql.Connection}
     * @param user - user String used to make a {@link a java.sql.Connection}
     * @param pass - password String used to make a {@link a java.sql.Connection}
     * @throws SQLException - if the properties do not pass sanity check by {@link #propertiesCheck()}
     *                        or failures occur while making a {@link java.sql.Connection}
     */
    public ConnectionPoolManager(PoolConfiguration props, String url, String user, String pass) throws SQLException {
        this.props = props;
        this.url = url;
        this.user = user;
        this.pass = pass;
        this.initializePool();
    }

    /**
     * Constructor with a given {@link java.util.Properties}
     *
     * @param props - {@link java.util.Properties} used to set this pool's properties
     * @param url - url String used to make a {@link a java.sql.Connection}
     * @param user - user String used to make a {@link a java.sql.Connection}
     * @param pass - password String used to make a {@link a java.sql.Connection}
     * @throws SQLException - if the properties do not pass sanity check by {@link #propertiesCheck()}
     *                        or failures occur while making a {@link java.sql.Connection}
     */
    public ConnectionPoolManager(Properties props, String url, String user, String pass) throws SQLException {
        this(new PoolProperties(props), url, user, pass);
    }

    /**
     * Constructor with default {@link PoolProperties}
     *
     * @param url - url String used to make a {@link a java.sql.Connection}
     * @param user - user String used to make a {@link a java.sql.Connection}
     * @param pass - password String used to make a {@link a java.sql.Connection}
     * @throws SQLException - if the properties do not pass sanity check by {@link #propertiesCheck()}
     *                        or failures occur while making a {@link java.sql.Connection}
     */
    public ConnectionPoolManager(String url, String user, String pass) throws SQLException {
        this(new PoolProperties(true), url, user, pass);
    }

    /**
     * Initialize the pool with {@link PoolProperties#initialSize} of connections
     * available to Clients and instantiate a {@link ConnectionReleaser} thread, if
     * {@link PoolProperties#runReleaser} is set to true.
     *
     * {@link #availableConnections} is set to {@link PoolProperties#intialSize} and
     * {@link #size} is incremented.
     *
     * @throws SQLException - if the properties do not pass sanity check by {@link #propertiesCheck()}
     *                        or failures occur while making a {@link java.sql.Connection}
     */
    protected void initializePool() throws SQLException {

        try {
            this.propertiesCheck();
        } catch (PoolConfigurationException e) {
            throw new SQLException("Failed to initialize a Connection Pool", e);
        }

        this.availableConnections =
            new ArrayBlockingQueue<Connection>(this.props.getMaxConnections(), true);
        this.busyConnections =
            new ArrayBlockingQueue<Connection>(this.props.getMaxConnections(), false);

        for (int i = 0; i < this.props.getInitialSize(); i++) {
            this.availableConnections.offer(this.createNewConnection());
            this.size.addAndGet(1);
        }
        if (this.props.getRunReleaser()) {
            releaser = new ConnectionReleaser(this);
            releaser.setBusyConnections(this.busyConnections);
            releaserThread = new Thread(releaser);
            releaserThread.start();
        } else {
            log.info("Not running ConnectionReleaser");
        }
    }

    /**
     * Sanity check for the current {@link PoolConfiguration} properties.
     *
     * <p>
     * {@link PoolConfiguration#getMaxConnections()} needs to be more than 0. {@link PoolConfiguration#getInitialSize()}
     * needs to be more than 0 and lesser than {@link PoolConfiguration#getMaxConnections()}. Note that
     * {@link PoolConfiguration#getMaxwait()} is in milliseconds (Typical values are from 10000 to 60000). If
     * {@link PoolConfiguration#getRunReleaser()} is true, i.e., when the {@link ConnectionReleaser} is set to run,
     * the interval between which it checks for closed connections by the clients must be atleast 3 times {@link PoolConfiguration#getMaxWait()},
     * so clients wait for enough time before a connection is released or size of the pool is decremented.
     * </p>
     *
     */
    protected void propertiesCheck() throws PoolConfigurationException {
        if (this.props == null) {
            throw new PoolConfigurationException("Properties for the pool needs to be defined using " + PoolConfiguration.class);
        }
        try {
            this.driver = (Driver) Class.forName(
                                                this.props.getDriverName(),
                                                true,
                                                this.getClass().getClassLoader()
                                                ).newInstance();
        } catch (Exception e) {
            throw new PoolConfigurationException(
                    "Driver " + this.props.getDriverName() + " cannot be loaded." +
                    "Make sure driver classpath is included.", e);
        }
        if (this.props.getMaxConnections() <= 0) {
            log.warn("Maximum connections in the properties is <= 0. Setting it to default: " + PoolProperties.DEFAULT_MAX_CONNECTIONS);
            this.props.setMaxConnections(PoolProperties.DEFAULT_MAX_CONNECTIONS);
        }
        if (this.props.getInitialSize() > this.props.getMaxConnections() || this.props.getInitialSize() < 0) {
            log.warn("Initial size is " + this.props.getInitialSize() + ". Setting it to default: " + PoolProperties.DEFAULT_INITIAL_SIZE);
            this.props.setInitialSize(PoolProperties.DEFAULT_INITIAL_SIZE);
        }
        if (this.props.getMaxWait() <= 10) {
            log.warn("Maximum wait to throw an exception is set to less than 10. Setting it to default: " + PoolProperties.DEFAULT_MAX_WAIT);
            this.props.setMaxWait(PoolProperties.DEFAULT_MAX_WAIT);
        }
        if (this.props.getRunReleaser() && this.props.getMaxWait() < (3 * this.props.getReleaserInterval())) {
            log.warn("Maximum wait to throw an exception is set to less than 3 * releaseInterval. Setting them to default");
            this.props.setMaxWait(PoolProperties.DEFAULT_MAX_WAIT);
            this.props.setReleaserInterval(PoolProperties.DEFAULT_RELEASER_INTERVAL);
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * When trying to get a connection, pool manager looks for an available valid connectioni and if available
     * returns immediately. If not, tries to create a new connection if the {@link #size} has not exceeded the
     * {@link PoolConfiguration#getMaxConnections()}. If unsuccessful, it waits for {@link PoolConfiguration#getMaxWait()}
     * milliseconds and throws a timed out {@link java.sql.SQLException} if unsuccessful again.
     * </p>
     *
     */
    @Override
    public Connection getConnection() throws SQLException {
        if (this.isClosed()) {
            throw new SQLException("Connection pool is closed");
        }
        // Immediately return if a connection is available
        Connection conn = this.waitAndGet(0);
        if (conn != null) {
            // w00t!!
            return conn;
        } else {
            // Create a new connection iff pool-capacity not exceeded.
            conn = this.createAndAdd();
            if (conn != null) {
                return conn;
            } else {
                // Wait maxWait seconds for other threads to release a connection
                long start = System.currentTimeMillis();
                conn = this.waitAndGet(this.props.getMaxWait());
                if (conn == null) {
                    if (System.currentTimeMillis() - start >= this.props.getMaxWait()) {
                        if (log.isDebugEnabled()) {
                            log.debug(this.capacityInfo("Timed out.", "\n"));
                        }
                        throw new SQLException("Timed out. No available connection after waiting for " + (this.props.getMaxWait()/1000) + " seconds.");
                    }
                }
            }
        }
        // check if pool is closed in the middle of the retrieval
        if (this.isClosed()) {
            this.disconnect(conn);
            throw new SQLException("Connection pool is closed");
        } else {
            return conn;
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * If the {@link ConnectionPoolManager} is closed or If {@link #busyConnections} does not
     * contain the passed {@link java.sql.Connection}, just disconnects the connection and returns.
     * If successfully removed, offers it to the {@link #availableConnections} and returns.
     * Atomically decrements {@link #size} iff the connection is removed and not added to
     * {@link #availableConnections}
     * </p>
     *
     */
    @Override
    public void releaseConnection(Connection connection) throws SQLException {
        if (connection == null) {
            return;
        }
        if (this.isClosed()) {
            this.disconnect(connection);
            return;
        }
        boolean closeConnection = true;
        if (this.busyConnections.remove(connection)) {
            // Connection belongs to the pool. Decrement pool size
            if (this.size.get() > this.props.getMaxConnections()) {
                if (log.isDebugEnabled()) {
                    log.debug(this.capacityInfo("Maximum connections size exceeded. Cannot release Connection[" + connection + "] to the pool. Closing it.", "\n"));
                }
                this.size.decrementAndGet();
            } else if (!this.availableConnections.offer(connection)) {
                // Capacity exceeded?
                if (log.isDebugEnabled()) {
                    log.debug(this.capacityInfo("Available connections size exceeded. Cannot release Connection[" + connection + "] to the pool. Closing it.", "\n"));
                }
                this.size.decrementAndGet();
            } else {
                if (log.isDebugEnabled()) {
                    log.debug(this.capacityInfo("Fine. Released Connection[" + connection + "] to the pool.", "\n"));
                }
                // everything went fine. Connection released to the pool.
                closeConnection = false;
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug(this.capacityInfo("Failed to release a connection (Does not belong to the pool). Connection [" + connection + "] will be closed", "\n"));
            }
        }
        if (closeConnection) {
            this.disconnect(connection);
        }
    }

    /**
     * Wait for an available valid connection and return one, if any.
     * If the connection is closed, try to reconnect it.
     * Otherwise remove it from busy queue and
     * return null, so the calling function can attempt to create a new one.
     *
     * @return {@link java.sql.Connection}
     */
    protected Connection waitAndGet(int wait) throws SQLException {
        Connection conn = null;
        try {
            conn = this.availableConnections.poll(wait, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            throw new SQLException("Connection pool wait interrupted before " + wait + " milliseconds");
        }
        boolean decrementIfNull = false;
        if (conn != null && conn.isClosed()) {
            decrementIfNull = true;
            conn = this.reconnect(conn);
            if (log.isDebugEnabled()) {
                log.info("Reconnected: " + conn);
            }
        }
        if (!this.offerToBusy(conn)) {
            conn = null;
        } else {
            if (log.isDebugEnabled()) {
                log.debug(this.capacityInfo("Connection[" + conn + "] added to busy.", "\n"));
            }
        }
        if (decrementIfNull && conn == null) {
            this.size.decrementAndGet();
        }
        return conn;
    }

    /**
     *  Creates a new connection iff the {@link #size} has not exceeded
     *  {@link PoolConfiguration#getMaxConnections} and tries to offer it
     *  to {@link #busyConnections}.
     *
     * @return a new {@link java.sql.Connection}, null if unsuccessful
     */
    protected Connection createAndAdd() throws SQLException {
        if (this.size.get() >= this.props.getMaxConnections()) {
            return null;
        }
        Connection conn = this.createNewConnection();
        if (!this.offerToBusy(conn)) {
            return null;
        } else {
            this.size.addAndGet(1);
            return conn;
        }
    }

    /**
     * Tries to offer a connection to {@link #busyConnections}.
     * This happens only when a new connection is created or a connection is
     * obtained from {@link #availableConnections} after polling.
     *
     * @param conn A connection to be aded to busy queue.
     * @return true if added to busy queue
     */
    private boolean offerToBusy(Connection conn) throws SQLException  {
        if (conn == null || conn.isClosed()) {
            return false;
        }
        if (!this.busyConnections.offer(conn)) {
            if (log.isDebugEnabled()) {
                log.debug("Capacity exceeded!!");
            }
            return false;
        } else {
            return true;
        }
    }

    /**
     * Tries to reconnect using the {@link java.sql.Driver} {@link #driver}
     * using the {@link PoolConfiguration#getURLProperties()}.
     *
     * This is so that pool manager doesn't have to poll again for a connection.
     *
     * @param conn Connection that needs to be closed
     * @return a new {@link java.sql.Connection}
     */
    protected Connection reconnect(Connection conn) throws SQLException {
        this.disconnect(conn);
        conn = null;
        if (this.driver != null) {
            this.props.updateURLProperties(this.user, this.pass);
            try {
                conn = this.driver.connect(this.url, this.props.getURLProperties());
            } catch (SQLException e) {
                log.error("Failed to reconnect", e);
            }
            return conn;
        }
        return conn;
    }

    /**
     * Disconnect a connection.
     *
     * @param conn {@link java.sql.Connection} to be disconnected
     */
    protected boolean disconnect(Connection conn) throws SQLException {
        if (conn != null && !conn.isClosed()) {
            conn.close();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Create a brand new connection.
     *
     * @return {@link java.sql.Connection} a valid new connection.
     * @throws {@link java.sql.SQLException} if failure occurs while trying to get a connection.
     */
    protected Connection createNewConnection() throws SQLException {
        return DriverManager.getConnection(this.url, this.user, this.pass);
    }

    /**
     * Closes all available connections and clears all connections owned by this pool.
     *
     * If {@link ConnectionReleaser} is set to run using {@link PoolConfiguration#setRunReleaser}
     * this method waits for {@link #releaserThread} to join here, so it's not stranded.
     */
    public void close() throws SQLException {
        if (this.isClosed()) {
            return;
        }
        this.closed.set(true);
        this.size.set(this.props.getMaxConnections());

        if (this.props.getRunReleaser() && this.releaserThread != null && this.releaserThread.isAlive()) {
            log.debug("Waiting for Releaser to join");
            try {
                this.releaserThread.join();
            } catch (InterruptedException e) {
                log.error(e);
            }
            log.debug("Releaser joined");
        }

        BlockingQueue<Connection> pooledConnections = this.availableConnections;
        if (pooledConnections.size() == 0) {
            pooledConnections = busyConnections;
        }
        while (pooledConnections.size() > 0) {
            Connection conn = null;
            try {
                conn = pooledConnections.poll(1, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                if (log.isDebugEnabled()) {
                    log.debug(this.capacityInfo("Cannot close connection pool. Interrupted. Exception:\n" + e.getMessage(), "\n"));
                }
                throw new SQLException("Cannot close Connection Pool. Interrupted.", e);
            }
            if (pooledConnections == this.availableConnections) {
                this.disconnect(conn);
                if (pooledConnections.size() == 0) {
                    pooledConnections = busyConnections;
                }
            }
        }
    }

    /**
     * Returns the {@link PoolConfiguration} used by this pool
     *
     * @return props - Pool configuration this pool uses
     */
    public PoolConfiguration getProps() {
        return this.props;
    }

    /**
     * Returns the url used by this pool to make a {@link java.sql.Connection}
     *
     * @return url - url associated with this pool
     */
    public String getUrl() {
        return this.url;
    }

    /**
     * Returns the username used by this pool to make a {@link java.sql.Connection}
     *
     * @return user - user associated with this pool
     */
    public String getUser() {
        return this.user;
    }

    /**
     * Returns the password used by this pool to make a {@link java.sql.Connection}
     *
     * @return password - password associated with this pool
     */
    public String getPass() {
        return this.pass;
    }

    /**
     * Returns the driver used by this pool to make a {@link java.sql.Connection}.
     *
     * Returns null if the {@link ConnectionPoolManager} did not make any
     * {@link java.sql.Connection} at all right from the point of instantiation.
     *
     * @return driver - {@link java.sql.Driver} associated with this pool
     */
    public Driver getDriver() {
        return this.driver;
    }

    /**
     * Returns the size of this pool.
     *
     * @return size - Thread safe read on the {@link #size size} of the pool
     */
    public int getSize() {
        return this.size.get();
    }

    /**
     * See if the current pool is closed and not usable.
     * Thread safe read on the {@link #close close} of the pool
     *
     * @return true if the connection pool has been closed
     */
    public boolean isClosed() {
        return this.closed.get();
    }

    /**
     * Get number of busy/used connections in this pool
     *
     * @return int - number of busy connections
     */
    public int getBusySize() {
        return this.busyConnections.size();
    }

    /**
     * Get number of available connections from this pool
     *
     * @return int number of available connections
     */
    public int getAvailableSize() {
        return this.availableConnections.size();
    }

    /**
     * Check if a connection belongs to a pool
     *
     * @param Connection - connection to check
     * @return boolean true if the connection belongs to the pool
     */
    public boolean containsConnection(Connection connection) {
        if (this.busyConnections.contains(connection)) {
            return true;
        } else if (this.availableConnections.contains(connection))  {
            return true;
        }
        return false;
    }

    /**
     * Returns the capacity info: {@link #availableConnections}, {@link #busyConnections},
     * {@link PoolConfiguration#getMaxConnections()} and the current size of the pool {@link #size}
     *
     * Life saver!! (Helped a lot in debugging test issues)
     */
    public String capacityInfo(String prefix, String delimiter) {
        return prefix + delimiter +
            "\tCurrent Capacity: " + this.size.get() + "; Specified Capacity: " + this.props.getMaxConnections() + delimiter +
            "\tAvailable Connections: " + this.availableConnections.size() + "; Busy Connections: " + this.busyConnections.size();
    }
}
