package com.opower.connectionpool;

import java.sql.SQLException;

public class PoolConfigurationException extends SQLException {


    private static final long serialVersionUID = 7526472295622776147L;

    /**
     * Implement all Constructors in {@link java.sql.SQLException}
     */

    public PoolConfigurationException() {
    }

    public PoolConfigurationException(String msg) {
        super(msg);
    }

    public PoolConfigurationException(String msg, String sqlState) {
        super(msg, sqlState);
    }

    public PoolConfigurationException(String msg, String sqlState, int vendorCode) {
        super(msg, sqlState, vendorCode);
    }

    public PoolConfigurationException(String msg, String sqlState, int vendorCode, Throwable cause) {
        super(msg, sqlState, vendorCode, cause);
    }

    public PoolConfigurationException(String msg, String sqlState, Throwable cause) {
        super(msg, sqlState, cause);
    }


    public PoolConfigurationException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
