/**                 ***COPYRIGHT STARTS HERE***
* Copyright (C) 2009 Sun Microsystems, Inc. All rights reserved.
*
* SUN PROPRIETARY/CONFIDENTIAL.
*
* U.S. Government Rights - Commercial software. Government users are subject
* to the Sun Microsystems, Inc. standard license agreement and applicable
* provisions of the FAR and its supplements.
*
* Use is subject to license terms.
*
* This distribution may include materials developed by third parties.Sun,
* Sun Microsystems, the Sun logo, Java, Solaris and Sun Identity Manager
* are trademarks or registered trademarks of Sun Microsystems, Inc.
* or its subsidiaries in the U.S. and other countries.
*
* UNIX is a registered trademark in the U.S. and other countries, exclusively
* licensed through X/Open Company, Ltd.
*                  ***COPYRIGHT ENDS HERE***                                */
package org.identityconnectors.sap;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.identityconnectors.common.logging.Log;
import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.spi.AbstractConfiguration;

import com.sap.conn.jco.JCo;
import com.sap.conn.jco.JCoContext;
import com.sap.conn.jco.JCoDestination;
import com.sap.conn.jco.JCoDestinationManager;
import com.sap.conn.jco.JCoException;
import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.JCoRepository;
import com.sap.conn.jco.ext.DestinationDataEventListener;
import com.sap.conn.jco.ext.DestinationDataProvider;
import com.sap.conn.jco.ext.Environment;

/**
 * Class to represent a SAP Connection 
 *  
 * @author bfarrell
 * @version 1.0
 * @since 1.0
 */
public class SAPConnection { 

	protected SAPConnectConfiguration _configuration;
	protected JCoDestination _destination;
    protected boolean _jcoTrace = false;
    protected JCoRepository _repository;
    
    /**
     * Setup logging for the {@link SAPConnection}.
     */
    private static final Log log = Log.getLog(SAPConnection.class);
    
    public SAPConnection(SAPConnectConfiguration configuration) {
        _configuration = configuration;
    }
   

    /**
     * Create or modify a destination to manage the connection to the database.
     * @return JCoDestination
     * @throws ConnectorException
     */
    protected JCoDestination createDestination() throws ConnectorException {
    	log.info("BEGIN");
        JCoDestination dest = null;
        try {
            String destName = _configuration.getDestination();
            SAPDestinationDataProvider provider = SAPDestinationDataProvider.getProvider();

            if (provider.getDestinationProperties(destName) != null) {
                provider.changePropertiesForDestination(destName, _configuration);
            } else {
                provider.addDestination(destName, _configuration);
            }
            log.info(_configuration.getMessage("SAP_INFO_OBTAIN_DEST", destName));
            dest = JCoDestinationManager.getDestination(destName);
        } catch (Throwable t) {
            // for some reason, sometimes the destination does not exist.
            // retry creating the destination to fix
            if (t.getMessage().contains("does not exist")) {
                try {
                    String destName = _configuration.getDestination();
                    SAPDestinationDataProvider provider = SAPDestinationDataProvider.getProvider();
                    provider.changePropertiesForDestination(destName, null);
                    provider.addDestination(destName, _configuration);
                    dest = JCoDestinationManager.getDestination(destName);
                } catch (Throwable t2) {
                    String message = _configuration.getMessage("SAP_ERR_START_CONNECTION");
                    log.error(t, message);
                    throw new ConnectorException(t);
                }
            } else {
                String message = _configuration.getMessage("SAP_ERR_START_CONNECTION");
                log.error(t, message);
                throw new ConnectorException(t);
            }
        }
        log.info("RETURN");
        return dest;
    }
    
    /**
     * Make call to create the destination and then get the repository.
     * @param retry
     */
    public void connect() {
    	log.info("BEGIN");
        _destination = createDestination();
        if (_destination != null) {
            if (JCoContext.isStateful(_destination)) {
               	log.info(_configuration.getMessage("SAP_INFO_DEST_STATEFUL"));
                
            } else {
            	log.info(_configuration.getMessage("SAP_INFO_DEST_STATELESS"));
            	
            }
            try {
            	//log.error("Perf: Getting Repository Started "); 
                _repository = _destination.getRepository();
                //log.error("Perf: Getting Repository completed "); 
            } catch (JCoException jcoe) {
                _destination = null;
                log.error(jcoe, jcoe.getMessage());
                throw new ConnectorException(jcoe);
            }
        }
        log.info("RETURN");
    }
    
    public JCoRepository getRepository() {
        return _repository;
    }
    
    /**
     * Use this method to send BAPI to SAP.
     * @param funct
     */
    public void execute(JCoFunction funct) throws JCoException {
    	log.error("Perf: Execute funtion started for "+ funct.getName());
        funct.execute(_destination);
    	log.error("Perf: Execute funtion completed for "+ funct.getName());
    }
    
    /**
     * Release the client and set the repo to null.
     */
    public void dispose() {
    	log.info("BEGIN");
        try {
            if (_destination != null &&
                JCoContext.isStateful(_destination)) {
            	//log.error("Perf: Release destination started"); 
                JCoContext.end(_destination);
            	//log.error("Perf:  Release destination completed "); 
            }
        } catch (JCoException jcoe) {
            log.error(jcoe.getMessage());
            throw new ConnectorException(jcoe);
        }
        _destination = null;
        _repository = null;
        log.info("RETURN");
    }

    /**
     * Call the connect method and check to make sure the connection is alive.
     */
 
  /*  public void test() {
    	log.info("BEGIN");
        connect();
        try {
            _destination.ping();
        } catch (JCoException jcoe) {
            log.error(jcoe.getMessage());
            throw new ConnectorException(jcoe);
        }
        log.info("RETURN");
    }*/
    
    public void getStartContext() {
		log.info("BEGIN");
		JCoContext.begin(_destination);
		log.info("RETURN");
	}

	public void getEndContext() {
		log.info("BEGIN");
		try {
			JCoContext.end(_destination);
		} catch (JCoException jcoException) {
			log.error(_configuration.getMessage("SAP_ERR_JCO_CONTEXT_EXP",  jcoException));
		}
		log.info("RETURN");
	}

    
    /**
	 * This built in JCo class needs to be extended in order to maintain
	 * destinations.
	 */
    protected static class SAPDestinationDataProvider implements DestinationDataProvider {
        private DestinationDataEventListener _eL;
        
        private Map<String, Properties> _propertiesForDestinationName;
        
        private static SAPDestinationDataProvider _provider;
        
        /**
         * Get the JCo Destination Data Provider singleton, creating one if
         * needed.
         *
         * @return the current DestinationDataProvider
         */
        public static synchronized SAPDestinationDataProvider getProvider() {
        	log.info("BEGIN");
            if (_provider == null) {
                _provider = new SAPDestinationDataProvider();
                try {
                    Environment.registerDestinationDataProvider(_provider);
                } catch (IllegalStateException e) {
                    log.error(e.getMessage());
                }
            }
            log.info("RETURN");
            return _provider;
        }
        
        public SAPDestinationDataProvider() {
            _propertiesForDestinationName = new HashMap<String, Properties>();
        }

        public void addDestination(String destinationName, SAPConnectConfiguration config)
        {
            _propertiesForDestinationName.put(destinationName, createProviderProperties(config));
        }
        
        public Properties getDestinationProperties(String destinationName) {
            Properties props = null;
            if (destinationName != null) {
                props = _propertiesForDestinationName.get(destinationName);
            }
            return props;
        }
        
        void changePropertiesForDestination(String destinationName, SAPConnectConfiguration config)
        {
        	log.info("BEGIN");
            if(config==null)
            {
                _eL.deleted(destinationName);
                _propertiesForDestinationName.remove(destinationName);
            }
            else 
            {
                Properties properties = createProviderProperties(config);
                if(_propertiesForDestinationName!=null  && 
                   _propertiesForDestinationName.get(destinationName) != null &&
                   !_propertiesForDestinationName.get(destinationName).equals(properties)) {
                    _propertiesForDestinationName.put(destinationName, properties);
                    _eL.updated(destinationName);
                }
            }
            log.info("RETURN");
        }

        public void setDestinationDataEventListener(DestinationDataEventListener eventListener) {
            _eL = eventListener;
        }

        public boolean supportsEvents() {
            return true;
        }

        /**
         * Create the JCO properties to needed to create or update the destination.
         * @param config
         * @return Properties object for the destination
         * @throws ConnectorException
         */
        private Properties createProviderProperties(SAPConnectConfiguration config) throws ConnectorException {
        	log.info("BEGIN");
            Properties destProps = new Properties();
            final char [] _array = new char[50];
            destProps.put(JCO_CLIENT, config.getClient());
            //
            if(config.getAliasUser() != null && !config.getAliasUser().equalsIgnoreCase("none")){
            	destProps.put(JCO_ALIAS_USER, config.getAliasUser());
            }	
            if(config.getGatewayHost() != null && !config.getGatewayHost().equalsIgnoreCase("none")){
            	destProps.put(JCO_GWHOST, config.getGatewayHost());
            }
            if(config.getGatewayService() != null && !config.getGatewayService().equalsIgnoreCase("none")){
            	destProps.put(JCO_GWSERV, config.getGatewayService());
            }
            if(config.getTpHost() != null && !config.getTpHost().equalsIgnoreCase("none")){
            	destProps.put(JCO_TPHOST, config.getTpHost());
            }
            if(config.getTpName() != null && !config.getTpName().equalsIgnoreCase("none")){
            	destProps.put(JCO_TPNAME, config.getTpName());
            }
            if(config.getType() != null && !config.getType().equalsIgnoreCase("none")){
            	destProps.put(JCO_TYPE, config.getType());
            }	
            if(config.getCodePage() != null && !config.getCodePage().equalsIgnoreCase("none")){	
            	destProps.put(JCO_CODEPAGE, config.getCodePage());
            }
            if(config.getGetSSO2() != null && !config.getGetSSO2().equalsIgnoreCase("none")){		
            	destProps.put(JCO_GETSSO2, config.getGetSSO2());
            }
            if(config.getMySAPSSO2() != null && !config.getMySAPSSO2().equalsIgnoreCase("none")){
            	destProps.put(JCO_MYSAPSSO2, config.getMySAPSSO2());
            }
            if(config.getLCheck() != null && !config.getLCheck().equalsIgnoreCase("none")){
            	destProps.put(JCO_LCHECK, config.getLCheck());
            }	
           	/*if(config.getDsr() != null && !config.getDsr().equals("") && !config.getDsr().equalsIgnoreCase("none")){		
           		destProps.put(JCO_DSR, config.getDsr());
           	}*/
           	if(config.getRepositoryDestination() != null && !config.getRepositoryDestination().equalsIgnoreCase("none")){ 			
           		destProps.put(JCO_REPOSITORY_DEST, config.getRepositoryDestination());
           	}
            if(config.getRepositoryUser() != null && !config.getRepositoryUser().equalsIgnoreCase("none")){            
            	destProps.put(JCO_REPOSITORY_USER, config.getRepositoryUser());
            }
            if(config.getRepositoryPassword() != null && !config.getRepositoryPassword().equalsIgnoreCase("none")){            
            	destProps.put(JCO_REPOSITORY_PASSWD, config.getRepositoryPassword());
            }
            if(config.getRepositorySNCMode() != null && !config.getRepositorySNCMode().equalsIgnoreCase("none")){            	
            	destProps.put(JCO_REPOSITORY_SNC, config.getRepositorySNCMode());
            }
           
	    // Start:: Bug 19078269
	    boolean useLoadBalance = config.getLoadBalance();
	    // End:: Bug 19078269  

            if (useLoadBalance) {
                // These attribute are used only for Load Balancing to SAP Systems 
                destProps.put(JCO_R3NAME, config.getR3Name());
                destProps.put(JCO_MSHOST, config.getMsHost());
                // optional field
                String msServ = config.getMsServ();
                if (null != msServ)
                    destProps.put(JCO_MSSERV, msServ);
                destProps.put(JCO_GROUP, config.getJcoGroup());
            } else {
                // These attribute are used only for Direct Connection to the SAP system 
                destProps.put(JCO_ASHOST, config.getHost());
                destProps.put(JCO_SYSNR, config.getSystemNumber());
            }

            // If specified, use the SAP Router attribute
            String sapRouter = config.getJcoSAPRouter();
            if (sapRouter != null) {
                destProps.put(JCO_SAPROUTER, sapRouter);
            }

            boolean useSnc = config.getUseSNC();
            
            // START BUG : Bug 25407306 - SAP UM 11.1.1.7.0 WITH SNC NW7.5 NOT WORKING 
            if(config.getUser() != null && ! config.getUser().isEmpty())
            destProps.put(JCO_USER, config.getUser());
           // END BUG : Bug 25407306 - SAP UM 11.1.1.7.0 WITH SNC NW7.5 NOT WORKING             
            if (useSnc) {
            	destProps.put(JCO_SNC_MODE, "1");
                if(config.getSncPartnerName() != null && ! config.getSncPartnerName().isEmpty())
                   destProps.put(JCO_SNC_PARTNERNAME, config.getSncPartnerName());
                if(config.getSncName() != null && ! config.getSncName().isEmpty())
                   destProps.put(JCO_SNC_MYNAME, config.getSncName());
                if(config.getSncLib() != null && ! config.getSncLib().isEmpty())
            	   destProps.put(JCO_SNC_LIBRARY, config.getSncLib());
            	if(config.getSncProtectionLevel() != null && ! config.getSncProtectionLevel().isEmpty())
            	   destProps.put(JCO_SNC_QOP, config.getSncProtectionLevel());
            	if(config.getSncX509Cert() != null && ! config.getSncX509Cert().isEmpty())
                   destProps.put(JCO_X509CERT, config.getSncX509Cert());              
            } else {             
                config.getPassword().access(new GuardedString.Accessor() {
                	public void access(char[] clearChars) {
						try {
					        System.arraycopy(clearChars, 0, _array, 0, clearChars.length); 
							//destProps.put(JCO_PASSWD, new String(clearChars));	
						} catch (Exception sException) {
							log.error(sException.getMessage());
						}
                	}
              /*GuardedStringAccessor accessor = new GuardedStringAccessor();
                config.getPassword().access(accessor);
                String password = new String(accessor.getArray());
                accessor.clear();
                destProps.put(JCO_PASSWD, password);*/
                }); 	
             // START BUG : Bug 25407306 - SAP UM 11.1.1.7.0 WITH SNC NW7.5 NOT WORKING
                if(_array != null && _array.length >0)
                destProps.put(JCO_PASSWD, new String(_array).trim());
             // END BUG : Bug 25407306 - SAP UM 11.1.1.7.0 WITH SNC NW7.5 NOT WORKING
            }
            if(config.getLanguage() != null && ! config.getLanguage().isEmpty())
            destProps.put(JCO_LANG, config.getLanguage());
            
            // if these default to 0, we dont necessarily want to set them
            // if they are -1 then dont set and let them use the default
            if (config.getConfigureConnectionTuning()) {
                // Connection  Pool properties
                Integer intPoolProp = config.getConnectionPoolCapacity();
                if (intPoolProp != null &&
                    intPoolProp.intValue() >= 0) {
                    destProps.put(JCO_POOL_CAPACITY, intPoolProp.toString());
                }

                intPoolProp = config.getConnectionPoolActiveLimit();
                if (intPoolProp != null &&
                    intPoolProp.intValue() >= 0) {
                    destProps.put(JCO_PEAK_LIMIT, intPoolProp.toString());
                }

                intPoolProp = config.getConnectionPoolExpirationTime();
                if (intPoolProp != null &&
                    intPoolProp.intValue() >= 0) {
                    destProps.put(JCO_EXPIRATION_TIME, intPoolProp.toString());
                }

                intPoolProp = config.getConnectionPoolExpirationTime();
                if (intPoolProp != null &&
                    intPoolProp.intValue() >= 0) {
                    destProps.put(JCO_EXPIRATION_PERIOD, intPoolProp.toString());
                }

                intPoolProp = config.getConnectionMaxGetTime();
                if (intPoolProp != null &&
                    intPoolProp.intValue() >= 0) {
                    destProps.put(JCO_MAX_GET_TIME, intPoolProp.toString());
                }
            }
            destProps.put(JCO_USE_SAPGUI, "0"); //prevent use of SAP GUI
            
            Integer traceLevel = config.getJcoTrace();
            if (traceLevel > 0) {
                String tracePath = config.getJcoTraceDir();
                JCo.setTrace(traceLevel, tracePath);
            }
            destProps.put(JCO_TRACE, (traceLevel > 0) ? "1" : "0");
            // Automatically set the CPIC trace level based upon the trace level.
            destProps.put(JCO_CPIC_TRACE, Integer.toString(traceLevel/3));
            log.info("RETURN");
            return destProps;
        }
    }
        
}
