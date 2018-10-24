package org.identityconnectors.sap;

import java.util.HashSet;
import java.util.Set;

import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.OperationOptions;
import org.identityconnectors.framework.common.objects.Uid;
import org.identityconnectors.sap.Function;
import org.identityconnectors.sap.SAPConfiguration;
import org.identityconnectors.sap.SAPConnection;

import com.sap.conn.jco.JCoException;

public class Delete{
	
	 private static final Log log = Log.getLog(Delete.class);
	 private SAPConnection _connection;
	    private SAPConfiguration _configuration;
	    private  Set<String> _filteredAccounts = new HashSet<String>();
	    public Delete(SAPConnection conn, SAPConfiguration config, Set<String> filteredAccounts) {
	        _connection =conn;
	    	_configuration=config;
	    	 _filteredAccounts=filteredAccounts;
	    }

	    /**
	     * Execute the actual delete command.
	     * @param objClass
	     * @param uid
	     * @param options
	     */
	    public void execute(final ObjectClass objClass, final Uid uid, final OperationOptions options) {
	    	log.info("BEGIN");
	        if (objClass != null && !objClass.equals(ObjectClass.ACCOUNT))
	            throw new IllegalArgumentException(_configuration.getMessage("UNSUPPORTED_OBJECT_CLASS", objClass.getObjectClassValue()));
	        _connection.connect();
	        
	        String message =null;
	        String accountId = uid.getUidValue();
	        
	        message = _configuration.getMessage("SAP_INFO_DELETING_USR", accountId);
	        log.info(message);
	       	        
	        if (_filteredAccounts.contains(accountId)) {
	            message = _configuration.getMessage("SAP_ERR_DELETE_FILTERED_ACCT", accountId);
	            log.error(message);
	            throw new ConnectorException(message);
	        }
	        
	        try {
	            Function function = new Function("BAPI_USER_DELETE", accountId, _connection, _configuration.getConnectorMessages(),
	                                             _configuration.getEnableCUA(), _configuration.getRetryWaitTime());
	            function.executeWithRetry(_configuration.getMaxBAPIRetries());
	            function.jcoErrorCheck();
	            boolean throwNotExists = options != null && options.getOptions() != null && options.getOptions().get("throwNotExists") != null ? 
	                                     (Boolean) options.getOptions().get("throwNotExists") : true;
	            if (throwNotExists) {
	                function.jcoUserNotExistCheck();
	            }
	            
	        } catch (JCoException ex) {
	            message = _configuration.getMessage("SAP_ERR_DELETE_USER", accountId);
	            log.error(message);
	            ConnectorException ce = new ConnectorException(message, ex);
	            throw ce;
	        } 
	        log.info("RETURN");
	    } 

}
