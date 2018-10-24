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

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeInfo;
import org.identityconnectors.framework.common.objects.AttributeUtil;
import org.identityconnectors.framework.common.objects.Name;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.OperationOptions;
import org.identityconnectors.framework.common.objects.ResultsHandler;
import org.identityconnectors.framework.common.objects.Schema;
import org.identityconnectors.framework.common.objects.SyncResultsHandler;
import org.identityconnectors.framework.common.objects.SyncToken;
import org.identityconnectors.framework.common.objects.Uid;
import org.identityconnectors.framework.common.objects.filter.FilterTranslator;
import org.identityconnectors.framework.spi.AttributeNormalizer;
import org.identityconnectors.framework.spi.Configuration;
import org.identityconnectors.framework.spi.Connector;
import org.identityconnectors.framework.spi.ConnectorClass;
import org.identityconnectors.framework.spi.PoolableConnector;
import org.identityconnectors.framework.spi.operations.CreateOp;
import org.identityconnectors.framework.spi.operations.DeleteOp;
import org.identityconnectors.framework.spi.operations.SchemaOp;
import org.identityconnectors.framework.spi.operations.SearchOp;
import org.identityconnectors.framework.spi.operations.SyncOp;
import org.identityconnectors.framework.spi.operations.TestOp;
import org.identityconnectors.framework.spi.operations.UpdateOp;
import org.identityconnectors.framework.spi.operations.UpdateAttributeValuesOp;
import com.sap.conn.jco.JCoException;



/**
 * Main implementation of the SAP Connector
 * 
 * @author bfarrell
 * 
 * @version 1.0
 */


@ConnectorClass(displayNameKey = "SAP", configurationClass = SAPConfiguration.class)
public class SAPConnector implements PoolableConnector, DeleteOp,
		SearchOp<String>,SyncOp, UpdateOp, CreateOp, TestOp, SchemaOp, UpdateAttributeValuesOp, AttributeNormalizer {
	/**
	 * Setup logging for the {@link SAPConnector}.
	 */
	private static final Log log = Log.getLog(SAPConnector.class);

	/**
	 * Place holder for the Connection created in the init method
	 */
	private SAPConnection _connection;

	/**
	 * Place holder for the {@link Configuration} passed into the init() method
	 * {@link SAPConnector#init}.
	 */
	private SAPConfiguration _configuration;

	private Schema _schema;
	private Map<String, AttributeInfo> _accountAttributes;

	public static final String USERNAME = "USERNAME";
	public static final String USERPASS = "PASSWORD";
	public static final String DELIMITER = ";";
	public static final String COMPLEX_DELIMITER = "|:";
	// since we are using String.split() and "|" is a special character
	// need to put "\\" in front as an escape sequence
	public static final String ESCAPED_COMPLEX_DELIMITER = "\\" + COMPLEX_DELIMITER;

	/**
	 * This is a list of non-String attributes. They need to be handled
	 * correctly in the create and update.
	 */
	public static Set<String> _specialAttributes = new HashSet<String>();
	static {
		_specialAttributes.add(SAPConstants.ATTR_CUA_SYSTEMS);
		_specialAttributes.add(SAPConstants.ATTR_PROFILES);
		_specialAttributes.add(SAPConstants.ATTR_ACTIVITY_GROUPS);
		_specialAttributes.add(SAPConstants.AGR_FROM_DAT);
		_specialAttributes.add(SAPConstants.AGR_TO_DAT);
		_specialAttributes.add(SAPConstants.ATTR_ROLES_EMBEDED);
		_specialAttributes.add(SAPConstants.ATTR_PROFILES_EMBEDED);
		_specialAttributes.add(SAPConstants.ATTR_PARAMETERS_EMBEDED);
		_specialAttributes.add(SAPConstants.ATTR_GROUPS_EMBEDED);
		_specialAttributes.add(SAPConstants.PERSONNEL_NUMBER);
		//_specialAttributes.add(SAPConstants.ATTR_ACCOUNT_LOCKED_NO_PWD);
		//_specialAttributes.add(SAPConstants.ATTR_ACCOUNT_LOCKED_WRNG_PWD);
	}

	/**
	 * This is a list of accounts that cannot be modified by the connector.
	 */
	private Set<String> _filteredAccounts = new HashSet<String>();
	

	/**
	 * List of formats for tables to be used in the connector.
	 */
	private Map<String, String> _tableFormats = new HashMap<String, String>();

	/**
	 * Gets the Configuration context for this connector.
	 */
	public Configuration getConfiguration() {
		return this._configuration;
	}

	public SAPConnection getConnection() {
		return this._connection;
	}

	/**
	 * Callback method to receive the {@link Configuration}.
	 * 
	 * @see Connector#init
	 */
	public void init(Configuration cfg) {
		log.info("Call init.");
		this._configuration = (SAPConfiguration) cfg;
		this._connection = new SAPConnection(this._configuration);
		this._filteredAccounts.addAll(Arrays.asList(_configuration
				.getFilteredAccounts()));
		for (String tableFormat : _configuration.getTableFormats()) {
			String[] tableDef = tableFormat.split(":=");
			if (tableDef == null || (tableDef != null && tableDef.length != 2)) {
				String message = _configuration.getMessage(
						"SAP_ERR_TABLE_FORMAT", tableFormat);
				log.error(message);
				throw new ConnectorException(message);
			}
			this._tableFormats.put(tableDef[0], tableDef[1]);
		}
		
		log.info("Begin Validate");
		_configuration.validate();
		log.info("End Validate");
		
		_connection.connect();	
		
	}

	/**
	 * Disposes of the {@link testerConnector}'s resources.
	 * 
	 * @see Connector#dispose()
	 */
	public void dispose() {
		_configuration = null;
		if (_connection != null) {
			_connection.dispose();
			_connection = null;
		}
		_schema = null;
		_accountAttributes = null;
	}
	
	
	public void checkAlive() {
		
		log.info("BEGIN");
		
		try {	
        	//log.error("Perf:  Ping destination started ");
			_connection._destination.ping();	
        	//log.error("Perf:  Ping destination completed ");
		} 
		 catch (JCoException jcoe) {
			log.error(jcoe.getMessage());
            throw new ConnectorException(jcoe);
         } 
          	
		 log.info("RETURN");
	}
	
	

	/******************
	 * SPI Operations
	 * 
	 * Implement the following operations using the contract and description
	 * found in the Javadoc for these methods.
	 ******************/

	/**
	 * {@inheritDoc}
	 */
	public Uid create(final ObjectClass objClass, final Set<Attribute> attrs,
			final OperationOptions options) {
		//Schema schema = schema();
		Uid uid = null;
		log.error("Perf: Create Entered for user {0}", AttributeUtil.getNameFromAttributes(attrs).getNameValue());
		uid = new Create( _connection,_configuration,_accountAttributes,_filteredAccounts,_tableFormats).execute(objClass, attrs, options);
		log.error("Perf: Create Exiting for user {0}", AttributeUtil.getNameFromAttributes(attrs).getNameValue());
		return uid;

	}

	/**
	 * {@inheritDoc}
	 */
	public void delete(final ObjectClass objClass, final Uid uid,
			final OperationOptions options) {
		log.error("Perf: Delete Entered for user {0}", uid.getUidValue());
		Delete delete = null;
		new Delete(_connection, _configuration, _filteredAccounts).execute(
						objClass, uid, options);		
		log.error("Perf: Delete Exiting for user {0}", uid.getUidValue());
	}


	/**
	 * {@inheritDoc}
	 */
	public FilterTranslator<String> createFilterTranslator(
			ObjectClass objClass, OperationOptions options) {
		return new SAPFilterTranslator();
	}

	/**
	 * {@inheritDoc}
	 */
	public void executeQuery(ObjectClass objClass, String query,
			ResultsHandler handler, OperationOptions options) {
		log.error("Perf: Search Execute query Entered");
		if (_accountAttributes == null) {
			schema();
		}
		new Query(_connection, _configuration,_accountAttributes,_filteredAccounts,_tableFormats).executeQuery(objClass,
				query, handler, options);
		log.error("Perf: Search Execute query Exiting");
    }

	/**
	 * {@inheritDoc}
	 */
	//  Bug 18894709: Modified to invoke connection test()
	public void test() {
		//log.error("Perf: Test connection Entered");
		checkAlive();
		//log.error("Perf: Test connection Exiting");		
	}

	private Uid update(final ObjectClass objClass, final Set<Attribute> attrs,
			final OperationOptions options) {
		Uid uid = null;
	 	log.error("Perf: Update Entered for user {0}", AttributeUtil.getUidAttribute(attrs).getUidValue()); 
					Update update = new Update( _connection,_configuration,_accountAttributes,_filteredAccounts,_tableFormats);
			uid = update.execute(objClass, attrs, options);
		log.error("Perf: Update Exiting for user {0}", AttributeUtil.getUidAttribute(attrs).getUidValue());
		return uid;

	}

	/**
	 * {@inheritDoc}
	 */
	public Uid update(ObjectClass obj, Uid uid, Set<Attribute> attrs,
			OperationOptions options) {
		return update(obj, AttributeUtil.addUid(attrs, uid), options);
	}


	
	public Schema schema() {
		//log.error("Perf: Schema Entered");
		UserSchema uschema = new UserSchema();
		_schema = uschema.getSchema();
		_accountAttributes = uschema.getAccountAttributeMap();
		//log.error("Perf: Schema Exiting");
		return _schema;
	}

	public Attribute normalizeAttribute(ObjectClass oclass, Attribute attribute) {
		if (oclass.is(ObjectClass.ACCOUNT_NAME) && attribute.is(Name.NAME)) {
			String value = (String) attribute.getValue().get(0);
			// Since search does case sensitive comparison, should not do the
			// upper case conversion
			return new Name(value.trim());
		} else if (attribute.is(Uid.NAME)) {
			String value = (String) attribute.getValue().get(0);
			// Since search does case sensitive comparison, should not do the
			// upper case conversion
			return new Uid(value.trim());
		}
		return attribute;		
	}
	

	/**
	 * 
	 */
	public SyncToken getLatestSyncToken(ObjectClass arg0) {
		throw new ConnectorException("getLatestSyncToken operation not supported by SAP system");       
	}


	public void sync(ObjectClass objClass, SyncToken token,
			SyncResultsHandler handler, OperationOptions options) {
		log.error("Perf: Sync Entered");
		new Query(_connection, _configuration,_accountAttributes,
				_filteredAccounts,_tableFormats).executeSyncQuery(objClass,
				token, handler, options);
		log.error("Perf: Sync Exiting");
	}
	
	/**
	 * {@inheritDoc} This method gives newly added multi-value attribute
	 */
	public Uid addAttributeValues(ObjectClass objclass, Uid uid,
			java.util.Set<Attribute> valuesToAdd, OperationOptions options) {
		log.error("Perf: addAttributeValues Entered for user {0}", uid.getUidValue());  
		//Updated for Bug 19594110
		Uid sUid=null;
		Update update = new Update( _connection,_configuration,_accountAttributes,
				_filteredAccounts,_tableFormats);
		sUid=update.processChildTableData(uid, valuesToAdd, true);
		log.error("Perf: addAttributeValues Exiting for user {0}", uid.getUidValue()); 
		return sUid;
		
	}

	/**
	 * {@inheritDoc} This method gives only removed multi-value attribute
	 * 
	 */
    public Uid removeAttributeValues(ObjectClass objclass, Uid uid,
            java.util.Set<Attribute> valuesToRemove, OperationOptions options) {
	 log.error("Perf: removeAttributeValues Entered for user {0}", uid.getUidValue()); 
     valuesToRemove = AttributeUtil.addUid(valuesToRemove, uid);
     Name name = AttributeUtil.getNameFromAttributes(valuesToRemove);
     uid = AttributeUtil.getUidAttribute(valuesToRemove);
     String accountId = null;
     if (uid == null) {
            accountId = name.getNameValue();
     } else {
            accountId = uid.getUidValue();
     }
     SAPCreateUpdateBase createUpdate = new SAPCreateUpdateBase(_connection, _configuration,
                  _accountAttributes, _filteredAccounts, _tableFormats);
     List<Object> activityGroups = null;
     for (Attribute attr : valuesToRemove) {
            String key = attr.getName();
            log.info(_configuration.getMessage("SAP_INFO_KEY_AND_VALUES_TO_REMOVE", key,valuesToRemove));
            AttributeInfo attrInfo = null;
            if (_accountAttributes != null)
                  attrInfo = _accountAttributes.get(key);
            // if attrinfo is null we have to assume that it is
            // created
            // outside of the connector. therefor we have to assume
            // that it
            // is updateable
            if (key.equals("roles") || key.equals("profiles")
                         || key.equals("parameters") || key.equals("groups")) {                 
                  activityGroups = attr.getValue();
                  log.info(_configuration.getMessage("SAP_INFO_ATTR_ACTVITY_GROUPS", attr,activityGroups));
				  //log.error("Perf: Removing Embedded Attribute {0} for user {1}: " , attr, accountId);
                  createUpdate.removeAttributeValues(activityGroups, key,
                                accountId);
  			      //log.error("Perf: Removed Embedded Attribute {0} for user {1}: " , attr, accountId);
            }
     }
	 log.error("Perf: removeAttributeValues Exiting for user {0}", uid.getUidValue());   
     return new Uid(accountId);
}

}
