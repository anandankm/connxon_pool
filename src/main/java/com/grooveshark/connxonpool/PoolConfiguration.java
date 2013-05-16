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

import java.util.Properties;

/**
 * An interface to configure {@link ConnectionPoolManager} properties.
 *
 * Can be configured using {@link java.util.Properties} or set to defaults
 * in the constructor. Basically says how the {@link ConnectionPoolManager} should
 * operate.
 *
 * @author andy.compeer@gmail.com
 */
public interface PoolConfiguration {

    /**
     * Minimim default values a {@link java.sql.Driver} would expect to reconnect
     * and return a {@link java.sql.Connection}
     */
    public static final String RECONNECT_USER_PROP = "user";
    public static final String RECONNECT_PASSWORD_PROP = "password";

    /**
     * Drivername to be used by the pool to make a connection.
     * Make sure you have the corresponding jar file in the classpath.
     *
     * @param - driverName to be used by this pool
     * @example - com.mysql.jdbc.Driver
     */
    public void setDriverName(String driverName);

    /**
     * Drivername to be used by the pool to make a connection.
     * Make sure you have the corresponding jar file in the classpath.
     *
     * @return - driverName used by this pool
     * @example - com.mysql.jdbc.Driver
     */
    public String getDriverName();

    /**
     * Maximum number of connections that a {@link ConnectionPoolManager} can hold,
     * beyond which a client has to wait atleast {@link #setMaxWait()} milliseconds value.
     * Typical values are from 10-100
     *
     * @param - maxConnections for this pool
     */
    public void setMaxConnections(int maxConnections);

    /**
     * Maximum number of connections that a {@link ConnectionPoolManager} can hold,
     * beyond which a client has to wait atleast {@link #setMaxWait()} milliseconds value.
     * Typical values are from 10-100
     *
     * @return - maximum number of connections of this pool
     */
    public int getMaxConnections();

    /**
     * Initial number of connections created by the connection pool.
     * These number of connections are created once a {@link ConnectionPoolManager} is instantiated.
     * This is to ensure that once a {@link ConnectionPoolManager} is created, clients
     * can query for a connection and get a valid one immediately.
     *
     * @param - initialSize for this pool
     */
    public void setInitialSize(int initialSize);

    /**
     * Initial number of connections created by the connection pool.
     * These connections are created once a {@link ConnectionPoolManager} is instantiated.
     * This is to ensure that once a {@link ConnectionPoolManager} is created, clients
     * can query for a connection and get a valid one immediately.
     *
     * @return - initial size of this pool
     */
    public int getInitialSize();

    /**
     * Time in milliseconds {@link ConnectionPoolManager} waits for a connection
     * to be available before throwing an exception {@link java.sql.SQLException},
     * when maximum number of connections is reached.
     * Typical values are from 10,000 to 60,000
     *
     * @param - maxWait in milliseconds to be used by this pool
     */
    public void setMaxWait(int maxWait);

    /**
     * Time in milliseconds {@link ConnectionPoolManager} waits for a connection
     * to be available before throwing an exception {@link java.sql.SQLException},
     * when maximum number of connections is reached.
     * Typical values are from 10,000 to 60,000
     *
     * @return - maxWait in milliseconds used by this pool
     */
    public int getMaxWait();

    /**
     * Interval for the {@link ConnectionReleaser} to peek into
     * busy connections, if they are closed or not.
     * {@link #setRunReleaser()} needs to be set to true, in order for this
     * value to be effective.
     *
     * @param - releaserInterval in milliseconds to be used by this pool
     */
    public void setReleaserInterval(int releaserInterval);

    /**
     * Interval for the {@link ConnectionReleaser} to peek into
     * busy connections, if they are closed or not.
     * {@link #setRunReleaser()} needs to be set to true, in order for this
     * value to be effective.
     *
     * @return - releaserInterval in milliseconds used by this pool
     */
    public int getReleaserInterval();

    /**
     * Specifies whether to run {@link ConnectionReleaser}.
     *
     * @param - runReleaser boolean value. True if {@link ConnectionReleaser} needs
     * to be run.
     */
    public void setRunReleaser(boolean runReleaser);

    /**
     * Specifies whether to run {@link ConnectionReleaser}.
     *
     * @return - boolean value. True if {@link ConnectionReleaser} is running or needs
     * to be run.
     */
    public boolean getRunReleaser();

    /**
     * {@link java.util.Properties} required by the {@link ConnectionPoolManager}
     * to reconnect using {@link java.sql.Driver} and get a valid {@link java.sql.Connection}.
     *
     * @param properties {@link java.util.Properties} with atleaset username and password for reconnection.
     * @example
     * <code>
     *    Properties urlProperties = new Properties();
     *    urlProperties.setProperty(PoolConfiguration.RECONNECT_USER_PROP, "andy");
     *    urlProperties.setProperty(PoolConfiguration.RECONNECT_PASSWORD_PROP, "$andy#!");
     * </code>
     */
    public void setURLProperties(Properties properties);

    /**
     * {@link java.util.Properties} required by the {@link ConnectionPoolManager}
     * to reconnect using {@link java.sql.Driver} and get a valid {@link java.sql.Connection}
     *
     * @return properties used by the {@link ConnectionPoolManager} to reconnect
     */
    public Properties getURLProperties();

    /**
     * Set all the default properties for this pool.
     */
    public void setDefaults();


    /**
     * Set this configuration properties using {@link java.util.Properties}.
     *
     * @param - props: {@link java.util.Properties} to be used to set the pool properties
     */
    public void setUsingProperties(Properties props);

    /**
     * Update {@link #URLProperties} with user and password for reconnection
     *
     * @param user user to be set
     * @param pass password to be set
     */
    public void updateURLProperties(String user, String pass);

}
