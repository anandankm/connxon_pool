package com.opower.connectionpool;


public class PoolConfigurationException extends Exception {


    private static final long serialVersionUID = 7526472295622776147L;

    public PoolConfigurationException(String msg) {
        super(msg);
    }

    public PoolConfigurationException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
