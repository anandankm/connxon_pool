# Connection Pool (OPOWER Homework)

An implementation of connection pool. This provides a thread safe framework to use connections from a pool, maintained and recycled. This reduces
the latency in getting a connection from the time it was requested. Example usage of this framework would be (look at test implementations in `src/test/java/com/opower/connectionpool/ConnectionPoolManagerTest`),
    

    String testURL = "jdbc:mysql://localhost:3306/opower_connxn?allowMultiQueries=true";
    String testUser = "user"; // mysql user
    String testPass = "pass"; // mysql pass
    ConnectionPoolManager pool;
    Connection conn;
    PoolConfiguration props = new PoolProperties(true);
    try {
        pool = new ConnectionPoolManager(props, testURL,testUser, testPass);
        conn = pool.getConnection();
        // perform operations
        // ...
        conn.close(); // close the connection
        pool.close(); // close the pool
    } catch (SQLException e) {
        e.printStackTrace();
    }

In the above code, pool manager assumes the default properties. Look at default properties in `src/main/resources/pool.properties`
If <code>pool.releaseConnection(conn)</code> is used, connection <code>conn<code>will be released to the pool. 
    
Normally, clients can use the below methods:

    pool.getConnection();
    pool.releaseConnection(conn);
    pool.close();

Another useful method in the framework is <code>capacityInfo(String prefix, String delimiter)</code>, which returns the pool capacity info
with number of available connections, number of busy connections, current pool capacity and specified pool capacity, in a nice tab separated `String`.

## [Pool Properties][Pool Properties]

Connection Pool properties are specified in `src/main/resources/pool.properties`. These properties are specific to a single 
`ConnectionPoolManager` instance. Currently the properties supported are (keys are the properties and the values are default values):

    POOL_DRIVER_NAME=com.mysql.jdbc.Driver
    POOL_MAX_CONNECTIONS=100
    POOL_INITIAL_SIZE=20
    POOL_MAX_WAIT=30000
    POOL_RUN_RELEASER=true
    POOL_RELEASER_INTERVAL=5000

Let's look at what they represent.

1. `POOL_DRIVER_NAME` is the driver name to be used by an instance of Connection Pool to get a connection.
2. `POOL_MAX_CONNECTIONS` is the maximum number of connections that an instance of Connection Pool can hold
   at a particular time. Once this number is reached, connection pool needs to release a connection
   or one of the clients have to close their connection.
3. `POOL_INITIAL_SIZE`  is the initial number of connections that this Connection Pool
4. `POOL_MAX_WAIT` is the maximum time this Connection Pool would wait before it throws an `java.sql.SQLException`
   saying timed out, when trying to get a connection, if the size of the pool has reached `POOL_MAX_CONNECTIONS`
5. `POOL_RUN_RELEASER` accepts boolean strings case-insensitive `true`. Anyother string would make it false. When this
   is set to `true`, a ConnectionReleaser `java.util.Thread` is started which looks for any closed connections used by
   the clients and releases them to the pool, so the size of the pool is not maxed out.
6. `POOL_RELEASER_INTERVAL` is the time interval (in `milliseconds`) that the `ConnectionReleaser` instance would run
   to release closed connections to the pool.

## Connection Pool Homework Instructions

An important thing to note about testing this Connection Pool Scaffold is the setup.properties
file provided in `src/test/resources` directory, which contains some additional properties we
can specify for testing purposes alone.

It contains the following properties (keys are the properties and the values are default values). Lets look at them one by one. 

     TEST_DB=opower_connxn
     TEST_TABLE=opower_test
     TEST_DRIVER_NAME=com.mysql.jdbc.Driver
     TEST_URL=jdbc:mysql://localhost:3306/opower_connxn?allowMultiQueries=true
     TEST_USER=root
     TEST_PASSWORD=pass
     TEST_QUERY=select * from opower_test
     TEST_CHECK_ROW_VALUES=true
     TEST_DRIVER_NAME=com.mysql.jdbc.Driver
     TEST_DB_SETUP_URL=jdbc:mysql://localhost:3306?allowMultiQueries=true
     TEST_DB_SETUP_QUERY=DROP DATABASE IF EXISTS opower_connxn;\
                         CREATE DATABASE opower_connxn; USE opower_connxn;\
                         DROP TABLE IF EXISTS opower_test;\
                         CREATE TABLE opower_test (\
                                 Userid INT(11) UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY, \
                                 Name VARCHAR(64) NOT NULL DEFAULT '0' \
                                 )ENGINE=InnoDB DEFAULT CHARSET=utf8;\
                         INSERT INTO opower_test VALUES (1, 'Anandan'), (2, 'Opower');

1. `TEST_DB` is database we need for testing our Connection Pool Scaffold.
2. `TEST_TABLE` is the test table we are going to use.
3. `TEST_DRIVER_NAME` is the driver as specified before in [Pool Properties][Pool Properties]
     TEST_URL=jdbc:mysql://localhost:3306/opower_connxn?allowMultiQueries=true
     TEST_USER=root
     TEST_PASSWORD=pass
     TEST_QUERY=select * from opower_test
     TEST_CHECK_ROW_VALUES=true
     TEST_DRIVER_NAME=com.mysql.jdbc.Driver
     TEST_DB_SETUP_URL=jdbc:mysql://localhost:3306?allowMultiQueries=true
     TEST_DB_SETUP_QUERY=DROP DATABASE IF EXISTS opower_connxn;\
# HOMEWORK INSTRUCTIONS

This is a very basic scaffold project for you to work in for the connection pool homework assignment

## Instructions

Please clone the repository and deliver your solution via an archive format of your choice, including all project files, within 1 calendar week.

Write a connection pool class that implements this interface (it is also located in `src/main/java/com/opower/connectionpool/ConnectionPool.java`):

    public interface ConnectionPool {
        java.sql.Connection getConnection() throws java.sql.SQLException;
        void releaseConnection(java.sql.Connection con) throws java.sql.SQLException;
    }

While we know there are many production-ready implementations of connection pools, this assignment allows for a variety of solutions to a real-world problem.  Your solution will be reviewed by the engineers you would be working with if you joined OPOWER.  We are interested in seeing your real-world design, coding, and testing skills.

## Using this scaffold

This scaffold is provided to help you (and us) build your homework code.
We've included a `pom.xml`, which is a file used by [maven][maven] to build the project and run other commands.   It also contains
information on downloading dependent jars needed by your project.  This one contains JUnit, EasyMock and Log4J already, but feel free
to change it as you see fit.

    mvn compile      # compiles your code in src/main/java
    mvn test-compile # compile test code in src/test/java
    mvn test         # run tests in src/test/java for files named Test*.java


[maven]:http://maven.apache.org/
[Pool Properties]:#PoolProperties
