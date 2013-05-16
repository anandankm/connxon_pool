package com.grooveshark.connxonpool;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Basic functionalities required by an implementation of a jdbc connection pool.
 *
 * {@link #getConnection()} gets a connection {@link java.sql.Connection} from the pool
 * if one is available and {@link #releaseConnection()} releases a connection back to the pool.
 * Both throw an {@link java.sql.SQLException} if a failure occurs while getting a connection
 * or releasing one or if some of the necessary {@link PoolConfiguration} properties are not met.
 *
 *
 */
public interface ConnectionPool {

    /**
     * Gets a connection from the connection pool.
     *
     * @return a valid connection from the pool.
     */
    Connection getConnection() throws SQLException;

    /**
     * Releases a connection back into the connection pool.
     *
     * @param connection the connection to return to the pool
     * @throws java.sql.SQLException
     */
    void releaseConnection(Connection connection) throws SQLException;
}
