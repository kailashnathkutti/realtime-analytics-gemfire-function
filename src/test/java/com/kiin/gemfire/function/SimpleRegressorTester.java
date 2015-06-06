package com.kiin.gemfire.function;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.gemstone.gemfire.cache.execute.FunctionService;

/**
 * Unit test for SimpleRegressor.
 */
public class SimpleRegressorTester 
    
{
	@Rule
	public ExpectedException exception = ExpectedException.none();
	
	private SimpleRegressor simpleRegressor;
	
	/**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public SimpleRegressorTester( String testName )
    {
    	simpleRegressor = new SimpleRegressor();
    	
    }
    
    @Test
    public void testGemFireConnection()
    {
    	Assert.assertNotNull(simpleRegressor.connectToGemFire("", 0));
    }
    
    @Test (expected=Exception.class)
    public void testFunction()
    {
    	exception.expect(Exception.class);
    	FunctionService.registerFunction(simpleRegressor);
    }
    
   /* @Test
    public void properfileTest()
    {
    	Assert.assertEquals(ConfigUtil.getPropertyByName("decimalformat"),"##.000000000");
    }*/
}
