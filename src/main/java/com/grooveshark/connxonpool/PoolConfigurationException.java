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

import java.sql.SQLException;

/**
 * Exception to be thrown when there is any discrepancies in the properties
 * set in {@link PoolConfiguration}
 *
 * @author andy.compeer@gmail.com
 */

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
