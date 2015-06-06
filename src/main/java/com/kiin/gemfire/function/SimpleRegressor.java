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
	
	
	public static void main(String[] args) throws FileNotFoundException {
    	
    
		//For testing, read data from a file. Cannot mock ClientCache.  
		InputStream is = new FileInputStream(new File("/Users/kuttik/Downloads/geohash_regression_data.csv"));
    	BufferedReader br = new BufferedReader(new InputStreamReader(is));
    	DateTimeFormatter formatter = DateTimeFormatter.ofPattern(ConfigUtil.getPropertyByName("dateformat"));
    	
    	// For testing, created cache run time. This way of creating cache did not seem to work at execute method
        ClientCache cacheClientF = new ClientCacheFactory().addPoolServer("sgxxkuttikm1.corp.emc.com", 40411).set("log-level", "error").create();
    	QueryService queryService = cacheClientF.getQueryService();
    	Query q1 = queryService.newQuery("select * from /geohashregion where toString().contains('s031w')");

    	//Make sure items in the map is in ascending order of date
    	Map<LocalDateTime, String> dateMap = new TreeMap<LocalDateTime, String>(new Comparator<LocalDateTime>() {
    	    public int compare(LocalDateTime date1, LocalDateTime date2) {
    	        return date1.compareTo(date2);
    	    }
    	});
    	
    	//Negative sign and precision can be adjusted as per needs
    	DecimalFormat f = new DecimalFormat(ConfigUtil.getPropertyByName("decimalformat"));
    	f.setNegativePrefix("-");
		try {
			
			 SelectResults<String> tuples = (SelectResults<String>) q1.execute();
		        for (String tuple : tuples) {
		            dateMap.put(LocalDateTime.parse(tuple.split(",")[0],formatter),tuple);
		        }
		    	//Not sure why the cache need to be closed and opened again, but this is the only way I could get the code running
		        cacheClientF.close();
		         cacheClientF = new ClientCacheFactory().addPoolServer("sgxxkuttikm1.corp.emc.com", 40411).set("log-level", "error").create();
		     	Region regionRegression = cacheClientF.getRegion("/geohashregion_regression_prediction");       	

    	 
        SimpleRegression simpleRegression = new SimpleRegression();
        
    	Object []ObjArrayDependentVar = dateMap.values().toArray();
    	
    	for(int i=0;i<ObjArrayDependentVar.length;i++)
    		{
    		//Double.parseDouble(ObjArrayDependentVar[i].toString());
        	simpleRegression.addData(i,Double.parseDouble(ObjArrayDependentVar[i].toString().split(",")[5]));
        	System.out.println("ObjArrayDependentVar[i].toString().split "+ObjArrayDependentVar[i].toString().split(",")[5]);
    		}
      /* br.lines().map(inputDatamap).forEach(
    			//str -> System.out.println(str[0]+" "+str[1])
    			str -> simpleRegression.addData(str[0], str[1])   			
    			);*/
    	
        System.out.println("Start date unit at t = 0:");
        System.out.println("Intercept: " + f.format(simpleRegression.getIntercept()));
        System.out.println("Slope    : " + f.format(simpleRegression.getSlope()));
        //regionRegression.put("intercept", f.format(simpleRegression.getIntercept()));
        //regionRegression.put("slope", f.format(simpleRegression.getSlope()));   
        System.out.println("Region    : " +regionRegression.getName() );

		} catch (FunctionDomainException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TypeMismatchException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NameResolutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }


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
