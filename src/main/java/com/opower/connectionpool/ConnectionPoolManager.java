package com.opower.connectionpool;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.DriverManager;
import java.util.LinkedList;

/**
 * Connection pool manager 
 */
public class ConnectionPoolManager implements ConnectionPool {
    /**
     * URL with [host] [port] [database] eg., "jdbc:mysql://localhost:3306/beluga?autoReconnect=true"
     */
    private String url;
    public String getUrl() { return this.url; }
    /**
     * Username to connect to the database
     */
    private String user;
    public String getUser() { return this.user; }
    /**
     * Password to connect to the database
     */
    private String pass;
    public String getPass() { return this.pass; }

    /**
     * List of connections in the pool
     *
     * list of connections currently used by the clients
     * list of connections available to the clients
     */
    private LinkedList<Connection> usedConnections = new LinkedList<Connection>();
    private LinkedList<Connection> availableConnections = new LinkedList<Connection>();

    /**
     * Connections lock
     */
    private Object connxnsLock = new Object();
    private Object usedConnxnsLock = new Object();
    private Object availConnxnsLock = new Object();

    /**
     * Pool properties for this instance of {@link ConnectionPoolManager}
     */
    private PoolProperties props;
    public void setProps(PoolProperties props) { this.props = props; }
    public PoolProperties getProps() { return this.props; }

    /**
     * Constructor with a given {@link PoolProperties}
     */
    public ConnectionPoolManager(PoolProperties props, String url, String user, String pass) throws SQLException {
        this.props = props;
        this.url = url;
        this.user = user;
        this.pass = pass;
        this.createConnectionPool();
    }

    /**
     * Constructor with default {@link PoolProperties}
     */
    public ConnectionPoolManager(String url, String user, String pass) throws SQLException {
        this(new PoolProperties(true), url, user, pass);
    }

    /**
     * Initialize connection pool with {@link PoolProperties}.initialSize
     */
    private void createConnectionPool() throws SQLException {
        try {
            Class.forName(this.props.driverName);
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver name " + this.props.driverName + " cannot be loaded."
                    + "Make sure driver classpath is included.", e);
        }
        for (int i = 0; i < this.props.initialSize; i++) {
            this.availableConnections.add(this.createNewConnection());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Connection getConnection() throws SQLException {
        // Immediately return if a connection is available
        Connection conn = this.getFromAvailable();
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
                try {
                    this.waitForAvailable();
                } catch (InterruptedException e) {
                    throw new SQLException("Interrupted by someone.", e);
                }
                conn = this.getFromAvailable();
                if (conn != null) {
                    return conn;
                }
            }
        }
        if (conn == null) {
            // if for some reason. Yikes!
            throw new SQLException("Failed to retrieve a valid connection from the pool.");
        } else {
            return conn;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void releaseConnection(Connection connection) throws SQLException {
        if (connection == null) {
            return;
        }
        boolean notRemoved = false;
        synchronized(this.connxnsLock) {
            if (!this.usedConnections.remove(connection)) {
                notRemoved = true;
            } else {
                if (connection.isClosed()) {
                    connection = createNewConnection();
                }
                this.availableConnections.addFirst(connection);
            }
            this.connxnsLock.notifyAll();
        }
        if (notRemoved) {
            throw new SQLException("Failed to release connection. Connection is not being used by the pool.");
        }
    }

    /**
     * Client thread waits on connxnsLock for {@link PoolProperties}.maxWait milliseconds
     *
     * @exception SQLException if connxnsLock.wait({@link PoolProperties}.maxWait) exceeded.
     */
    public void waitForAvailable() throws InterruptedException, SQLException {
        long start = System.currentTimeMillis();
        synchronized(this.connxnsLock) {
            this.connxnsLock.wait(this.props.maxWait);
        }
        if (System.currentTimeMillis() - start >= this.props.maxWait) {
            throw new SQLException("No available connections in the pool for the past " + this.props.maxWait + " milliseconds.");
        }
    }

    /**
     * Create a brand new connection.
     *
     * @return {@link java.sql.Connection}
     */
    public Connection createNewConnection() throws SQLException {
        return DriverManager.getConnection(this.url, this.user, this.pass);
    }


    public Connection getFromAvailable() throws SQLException {
        Connection conn = null;
        synchronized(this.connxnsLock) {
            if (!this.availableConnections.isEmpty()) {
                conn = this.availableConnections.removeFirst();
                this.usedConnections.add(conn);
            }
        }
        return conn;
    }

    public Connection createAndAdd() throws SQLException {
        Connection conn = null;
        synchronized(this.connxnsLock) {
            int totalSize = this.usedConnections.size() + this.availableConnections.size();
            if ((this.props.maxConnections == -1) || (totalSize < this.props.maxConnections)) {
                conn = this.createNewConnection();
                this.usedConnections.add(conn);
            }
        }
        return conn;
    }

    /**
     * Asynchronous read on used connections size
     *
     * @return int - number of used connections
     */
    public int getNumUsedConnections() {
        return this.usedConnections.size();
    }

    /**
     * Asynchronous read on available connections size
     *
     * @return int - number of available connections
     */
    public int getNumAvailConnections() {
        return this.availableConnections.size();
    }

    /**
     * Asynchronous check if a connection belongs to a pool
     *
     * @param Connection - connection to check
     * @return boolean - belongs to the pool or not
     */
    public boolean containsConnection(Connection connection) {
        if (this.usedConnections.contains(connection)) {
            return true;
        } else if (this.availableConnections.contains(connection))  {
            return true;
        }
        return false;
    }

    /**
     * Close and Clear all connections owned by this pool
     */

    public boolean clear() throws SQLException {
        synchronized(this.connxnsLock) {
            if (!usedConnections.isEmpty()) {
                throw new SQLException("Connections are in use. Release them before clearing the connection pool");
            }
            for (Connection conn : this.availableConnections) {
                if (conn != null) {
                    conn.close();
                }
            }
            this.availableConnections.clear();
        }
        return true;
    }
}
