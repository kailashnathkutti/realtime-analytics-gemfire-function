package com.kiin.gemfire.function;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.math3.stat.regression.SimpleRegression;

import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.client.ClientCache;
import com.gemstone.gemfire.cache.client.ClientCacheFactory;
import com.gemstone.gemfire.cache.execute.FunctionAdapter;
import com.gemstone.gemfire.cache.execute.FunctionContext;
import com.gemstone.gemfire.cache.query.FunctionDomainException;
import com.gemstone.gemfire.cache.query.NameResolutionException;
import com.gemstone.gemfire.cache.query.Query;
import com.gemstone.gemfire.cache.query.QueryInvocationTargetException;
import com.gemstone.gemfire.cache.query.QueryService;
import com.gemstone.gemfire.cache.query.SelectResults;
import com.gemstone.gemfire.cache.query.TypeMismatchException;
public class SimpleRegressor extends FunctionAdapter {
    /**
	 * Code by: Kailash on 06/June/2015
	 * A simple in-memory regression for Gemfire using Apache commons Math libraries
	 */
	private static final long serialVersionUID = 8597645617504937074L;

     /*
      * At the moment this function only works on one region , s031w. Will fix this soon
      * @see com.gemstone.gemfire.cache.execute.FunctionAdapter#execute(com.gemstone.gemfire.cache.execute.FunctionContext)
      */
	@Override
	public void execute(FunctionContext arg0) {
	
    	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    	
    	Cache cacheClientF = CacheFactory.getAnyInstance();
    	Region regionRegression = cacheClientF.getRegion("geohashregion_regression_prediction");    	
    	QueryService queryService = cacheClientF.getQueryService();
    	Query q1 = queryService.newQuery("select * from /geohashregion where toString().contains('s031w')");
    	
    	
    	//Make sure items in the map is in ascending order of date
    	Map<LocalDateTime, String> dateMap = new TreeMap<LocalDateTime, String>(new Comparator<LocalDateTime>() {
    	    public int compare(LocalDateTime date1, LocalDateTime date2) {
    	        return date1.compareTo(date2);
    	    }
    	});
    	
    	DecimalFormat f = new DecimalFormat("##.000000000");
    	f.setNegativePrefix("-");
		try {
			 SelectResults<String> tuples = (SelectResults<String>) q1.execute();
		        for (String tuple : tuples) {
		            dateMap.put(LocalDateTime.parse(tuple.split(",")[0],formatter),tuple);
		        }
					
		} catch (FunctionDomainException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TypeMismatchException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NameResolutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (QueryInvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    
    	 
		SimpleRegression simpleRegression = new SimpleRegression();
        
    	Object []ObjArrayDependentVar = dateMap.values().toArray();
    	/* 
    	 * Assuming that vessel information is sorted based on time, index is being used as X and vessel speed is in Y
    	 * */
    	for(int i=0;i<ObjArrayDependentVar.length;i++)
    		{
        	simpleRegression.addData(i,Double.parseDouble(ObjArrayDependentVar[i].toString().split(",")[5]));
    		}
   
    	
        /*System.out.println("Start date unit at t = 0:");
        System.out.println("Intercept: " + f.format(simpleRegression.getIntercept()));
        System.out.println("Slope    : " + f.format(simpleRegression.getSlope()));*/
        regionRegression.put("intercept", f.format(simpleRegression.getIntercept()));
        regionRegression.put("slope", f.format(simpleRegression.getSlope()));    
        arg0.getResultSender().lastResult("Done. ");
	}

	@Override
	public String getId() {
		return getClass().getName();
	}
    	
    public ClientCache connectToGemFire(String regionserver,int port)
    {
    	ClientCache returnCache = null;
    	
    	returnCache = new ClientCacheFactory().addPoolServer(regionserver, port).set("log-level", "error").create();

     	return returnCache;
    }
    	
}
