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
    
Clients can use two methods getConnection()

## Connection Pool Homework Instructions.

1.  An important thing to note about testing this Connection Pool Scaffold is the setup.properties
    file provided in `src/test/resources` directory.


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

