package com.opower.connectionpool;

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
    public static String testDB = "opower_connxn";
    public static String testTable = "opower_test";
    public static String testURL = "jdbc:mysql://localhost:3306/opower_connxn?allowMultiQueries=true";
    public static String testQuery = "SELECT * FROM opower_test";
    public static String testUser = "";
    public static String testPass = "";
    public static String dbSetupURL = "";
    public static String dbSetupQuery = "INSERT INTO opower_test VALUES (1, 'Anandan'), (2, 'Opower')";
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
