package com.kiin.gemfire.function;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
/**
* @author Kailashnath Kutti - kkutti@pivotal.io
*
*/
public class ConfigUtil {
static Properties properties = new Properties();
static {
ClassLoader loader = Thread.currentThread().getContextClassLoader();
InputStream stream = loader.getResourceAsStream("gemfireconnection.properties");

try {
properties.load(stream);
} catch (IOException e) {
e.printStackTrace();
}
}
public static String getConfigBasePath() {
return properties.getProperty("configBasePath");
}
public static String getPropertyByName(String name) {
return properties.getProperty(name);
}
}