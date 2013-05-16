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

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.SQLException;

import java.util.Properties;
import java.util.Enumeration;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import org.apache.log4j.Logger;

import static org.junit.Assert.fail;

/**
 *  Helps in setting up {@link ConnectionPoolManagerTest}
 */
public class SetupHelper
{

    public static String propertiesFileName = "setup.properties";
    public static boolean checkRowValues = false;
    public static String testDriverName = "com.mysql.jdbc.Driver";
    public static String testDB = "gshark_connxn";
    public static String testTable = "gshark_test";
    public static String testURL = "jdbc:mysql://localhost:3306/gshark_connxn?allowMultiQueries=true";
    public static String testQuery = "SELECT * FROM gshark_test";
    public static String testUser = "";
    public static String testPass = "";
    public static String dbSetupURL = "";
    public static String dbSetupQuery = "INSERT INTO gshark_test VALUES (1, 'Anandan'), (2, 'Grooveshark')";
    public static Properties setupProperties;

    public static final Logger log = Logger.getLogger(SetupHelper.class);

    public static void getProperties() throws IOException {
        setupProperties= new Properties();
        InputStream is = SetupHelper.class.getClassLoader().getResourceAsStream(propertiesFileName);
        setupProperties.load(is);
        Enumeration enumeration = setupProperties.propertyNames();
        while (enumeration.hasMoreElements()) {
            String key = (String) enumeration.nextElement();
            if (key.equals("TEST_DB")) {
                testDB =  setupProperties.getProperty("TEST_DB");
            } else if (key.equals("TEST_TABLE")) {
                testTable =  setupProperties.getProperty("TEST_TABLE");
            } else if (key.equals("TEST_URL")) {
                testURL =  setupProperties.getProperty("TEST_URL");
            } else if (key.equals("TEST_QUERY")) {
                testQuery =  setupProperties.getProperty("TEST_QUERY");
            } else if (key.equals("TEST_USER")) {
                testUser =  setupProperties.getProperty("TEST_USER");
            } else if (key.equals("TEST_PASSWORD")) {
                testPass =  setupProperties.getProperty("TEST_PASSWORD");
            } else if (key.equals("TEST_DB_SETUP_URL")) {
                dbSetupURL =  setupProperties.getProperty("TEST_DB_SETUP_URL");
            } else if (key.equals("TEST_DB_SETUP_QUERY")) {
                dbSetupQuery =  setupProperties.getProperty("TEST_DB_SETUP_QUERY");
            } else if (key.equals("TEST_DRIVER_NAME")) {
                testDriverName =  setupProperties.getProperty("TEST_DRIVER_NAME");
            } else if (key.equals("TEST_CHECK_ROW_VALUES") &&
                    setupProperties.getProperty("TEST_CHECK_ROW_VALUES").equalsIgnoreCase("true")) {
                checkRowValues = true;
            }
        }
        if (testDriverName.length() == 0)  {
            testDriverName = PoolProperties.DEFAULT_DRIVERNAME;
            if (testURL.length() != 0 &&
                    !(testURL.contains("jdbc:mysql:"))) {
                fail("Your TEST_URL property: " + testURL +
                        " is not supported by the default driver: " + testDriverName);
            }
            if (dbSetupURL.length() != 0 &&
                    !dbSetupURL.contains("jdbc:mysql:")) {
                fail("Your DB_SETUP_URL property: " + dbSetupURL +
                        " is not supported by the default driver: " + testDriverName);
            }
        }
    }

    /**
     * Sets up the db for a test environment.
     * Runs if TEST_DB_SETUP_URL and TEST_DB_SETUP_QUERY set in setup.properties
     */
    public static void dbSetup() throws SQLException {
        try {
            Class.forName(testDriverName);
        } catch (ClassNotFoundException e) {
            log.error("Class not found", e);
            fail("Class not found: " + testDriverName + "." +
                    "Make sure you have the jar file in the classpath. Exception: " + e.getMessage());
        }

        if (dbSetupURL.length() != 0 &&
                dbSetupQuery.length() != 0) {
            try {
                log.info("Setting up DB");
                Connection conn = DriverManager.getConnection(dbSetupURL,
                                                              testUser,
                                                              testPass);
                conn.createStatement().execute(dbSetupQuery);
            } catch (SQLException e) {
                log.error("Failed to setup database", e);
                fail("Failed to setup database. Exception: " + e.getMessage());
            }
        }
    }

}
