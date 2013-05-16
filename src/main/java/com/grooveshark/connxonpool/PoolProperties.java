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
import org.apache.log4j.Logger;

/**
 * A {@link PoolConfiguration} implementation to configure
 * {@link ConnectionPoolManager} properties.
 *
 * These values can be configured from a properties file. Once the properties
 * are loaded into {@link java.util.Properties}, this object can instantiated
 * and passed on to a {@link ConnectionPoolManager}.
 *
 * @author andy.compeer@gmail.com
 *
 */
public class PoolProperties implements PoolConfiguration {

    /**
     * DEFAULT Values
     */
    public static final String DEFAULT_DRIVERNAME = "com.mysql.jdbc.Driver";
    public static final int DEFAULT_MAX_CONNECTIONS = 20;
    public static final int DEFAULT_INITIAL_SIZE = 10;
    public static final int DEFAULT_MAX_WAIT = 30000; // 30 seconds
    public static final int DEFAULT_RELEASER_INTERVAL = 20000; // 20 seconds
    public static final boolean DEFAULT_RUN_RELEASER = true;


    /**
     * Logger
     */
    public static final Logger log = Logger.getLogger(PoolProperties.class);

    private volatile String driverName;
    private volatile int maxConnections;
    private volatile int initialSize;
    private volatile int maxWait;
    private volatile int releaserInterval;
    private volatile boolean runReleaser;
    private volatile Properties URLProperties;

    /**
     * Constructor with default properties for the pool
     *
     * @param - setDefaultValues: boolean value if true, sets the default properties
     */
    public PoolProperties(boolean setDefaultValues) {
        if (setDefaultValues) {
            this.setDefaults();
        }
    }

    /**
     * Constructor using {@link java.util.Properties}
     *
     * @param - props: {@link java.util.Properties} to be used to set the pool properties
     */
    public PoolProperties(Properties props) {
        this.setUsingProperties(props);
    }

    /**
     * Blank Constructor
     */
    public PoolProperties() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDriverName() {
        return this.driverName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setMaxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getMaxConnections() {
        return this.maxConnections;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setInitialSize(int initialSize) {
        this.initialSize = initialSize;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getInitialSize() {
        return this.initialSize;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setMaxWait(int maxWait) {
        this.maxWait = maxWait;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getMaxWait() {
        return this.maxWait;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setReleaserInterval(int releaserInterval) {
        this.releaserInterval = releaserInterval;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getReleaserInterval() {
        return this.releaserInterval;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setRunReleaser(boolean runReleaser) {
        this.runReleaser = runReleaser;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getRunReleaser() {
        return this.runReleaser;
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public void setURLProperties(Properties properties) {
        this.URLProperties = properties;
    }


    /**
     * {@inheritDoc}
     */
    public void updateURLProperties(String user, String pass) {
        if (this.URLProperties != null) {
            this.URLProperties = new Properties(this.URLProperties);
        } else {
            this.URLProperties = new Properties();
        }
        this.URLProperties.setProperty(PoolConfiguration.RECONNECT_USER_PROP, user);
        this.URLProperties.setProperty(PoolConfiguration.RECONNECT_PASSWORD_PROP, pass);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Properties getURLProperties() {
        return this.URLProperties;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDefaults() {
        this.driverName = DEFAULT_DRIVERNAME;
        this.maxConnections = DEFAULT_MAX_CONNECTIONS;
        this.initialSize = DEFAULT_INITIAL_SIZE;
        this.maxWait = DEFAULT_MAX_WAIT;
        this.releaserInterval = DEFAULT_RELEASER_INTERVAL;
        this.runReleaser = DEFAULT_RUN_RELEASER;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setUsingProperties(Properties props) {
        this.driverName = props.getProperty("POOL_DRIVER_NAME", DEFAULT_DRIVERNAME);
        this.maxConnections = Integer.parseInt(
                props.getProperty("POOL_MAX_CONNECTIONS", "" + DEFAULT_MAX_CONNECTIONS));
        this.initialSize = Integer.parseInt(
                props.getProperty("POOL_INITIAL_SIZE", "" + DEFAULT_INITIAL_SIZE));
        this.maxWait = Integer.parseInt(
                props.getProperty("POOL_MAX_WAIT", "" + DEFAULT_MAX_WAIT));
        this.releaserInterval = Integer.parseInt(
                props.getProperty("POOL_RELEASER_INTERVAL", "" + DEFAULT_RELEASER_INTERVAL));
        this.runReleaser = Boolean.parseBoolean(
                props.getProperty("POOL_RUN_RELEASER", (DEFAULT_RUN_RELEASER?"true":"false")));
    }

}
