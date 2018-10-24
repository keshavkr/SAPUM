package org.identityconnectors.sap.test.util;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.List;

import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.api.APIConfiguration;
import org.identityconnectors.framework.api.ConfigurationProperties;
import org.identityconnectors.framework.api.ConnectorFacade;
import org.identityconnectors.framework.api.ConnectorFacadeFactory;
import org.identityconnectors.framework.api.ConnectorInfo;
import org.identityconnectors.framework.api.ConnectorInfoManager;
import org.identityconnectors.framework.api.ConnectorInfoManagerFactory;
import org.identityconnectors.sap.SAPConfiguration;
import org.identityconnectors.sap.SAPConnector;

import org.identityconnectors.test.common.TestHelpers;


public final class ICFTestHelper {	
	
	private boolean connectionFailed = false;	
    private ConnectorFacade _facade = null;    
    private Log LOGGER = Log.getLog(getClass());
    private final static String _bundleLoc = "D:\\Oracle\\uploadJars\\org.identityconnectors.sap-2.0.0.jar";
    
    
    
    public static ICFTestHelper getInstance() {
        return new ICFTestHelper();
    }
    
    private ICFTestHelper() {
    }
    
    
    public static ConnectorFacade newFacade() {
		SAPUMTestUtil sumtu = new SAPUMTestUtil();
		SAPConfiguration conf=sumtu.newConfiguration();
		return newFacade(conf);	
		
	}
	
	public static ConnectorFacade newFacade(SAPConfiguration cfg) {
		ConnectorFacadeFactory factory = ConnectorFacadeFactory.getInstance();
		APIConfiguration impl = TestHelpers.createTestConfiguration(SAPConnector.class, cfg);
		return factory.newInstance(impl);
	}	
     
    public ConnectorFacade getFacade() {

        if (connectionFailed)
            throw new IllegalStateException("Connection to target failed");

        if (_facade == null) {
            initializeFacade();
        }
        return _facade;
    }
        
     
    private void initializeFacade() {
    
    LOGGER.info("Loading the bundle " + _bundleLoc);
	File bundleJarFile = new File(_bundleLoc);	

	if (!bundleJarFile.exists()) {
	throw new IllegalArgumentException("Bundle jar doesn't exist at "
	+ _bundleLoc);
	}

	try {
	URL connectorBundle = bundleJarFile.toURI().toURL();
	LOGGER.info("Bundle location: " + _bundleLoc);
	ConnectorInfoManagerFactory infoManagerFactory = ConnectorInfoManagerFactory.getInstance();
	ConnectorInfoManager infoManager = infoManagerFactory.getLocalManager(connectorBundle);
	
	       // Only one bundle should be loaded
			List<ConnectorInfo> connectorInfos = infoManager
			.getConnectorInfos();
			if (connectorInfos.size() != 1) {
				LOGGER.error(
			"Connector cannot be loaded. Only one bundle should be present."
			+ " {0} bundles found", connectorInfos.size());
			throw new IllegalStateException("Improper bundle found");
			}
			 // Get configuration props
            APIConfiguration apiConfig = connectorInfos.get(0)
                    .createDefaultAPIConfiguration();

            // Set configuration props
            ConfigurationProperties configProps = apiConfig
                    .getConfigurationProperties();             
            
            setSapConfigProps(configProps);
            
            _facade = ConnectorFacadeFactory.getInstance().newInstance(
                    apiConfig);
            LOGGER.info("Connector validated");           

        } catch (Exception e) {
        	LOGGER.error("Unable to initialize the bundle at {0}", _bundleLoc);
            connectionFailed = true;
            throw new IllegalStateException(e);
        }
    }
		
    public void setSapConfigProps(ConfigurationProperties sapConfigProps) {
	   
	   Object propertyvalue = null;
	   String methodName=null;
	   String propertyName=null;
	   String firstchar=null;	 
	   Object noparams[] = {};
	   
	   SAPUMTestUtil sumtu = new SAPUMTestUtil();
	   SAPConfiguration config=sumtu.newConfiguration();
	   
	   Method[] methods = config.getClass().getMethods();
	   
	   for (Method method: methods) {
		   if(method.getName().startsWith("get")) {
			   
			   methodName=new String(method.getName().substring(3));
			  
		   } else if(method.getName().startsWith("is")) {
			   methodName=new String(method.getName().substring(2));
			   	   
		   } else{
			   continue;
		   }
		   
		   if(methodName.equalsIgnoreCase("lcheck")){
			   propertyName=methodName;
		   } else {
			   firstchar= methodName.toLowerCase().substring(0, 1);
			   propertyName = firstchar.concat(methodName.substring(1)); 
		   }
		   
		   if(sapConfigProps.getPropertyNames().contains(propertyName)){
			   try{					  
				   propertyvalue= method.invoke(config, noparams);
				   sapConfigProps.setPropertyValue(propertyName, propertyvalue);
			   }catch(Exception e){
				   System.out.println(e.getMessage());
				   System.out.println("propertyName:"+propertyName);
				   System.out.println("propertyvalue:"+propertyvalue);
			   }
		   }
	  }		   
	 
   }   
  
}
