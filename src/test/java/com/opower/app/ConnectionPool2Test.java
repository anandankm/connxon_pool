package com.opower.connectionpool;

import org.junit.Test;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;

/**
 * Unit test for simple App.
 */
public class ConnectionPool2Test
{
    @BeforeClass
    public void setup()
    {
        System.out.println("in setup function");
    }

    @AfterClass
    public void teardown()
    {
        System.out.println("in tear down");
    }

    @Test
    public void testCase1()
    {
        System.out.println("Test case 1");
        assertEquals(new Long(2l), new Long(2l));
    }

    @Test
    public void testCase2()
    {
        System.out.println("Test case 2");
        assertEquals(2, 2);
    }
}
