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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.identityconnectors.common.StringUtil;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.framework.common.exceptions.AlreadyExistsException;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeBuilder;
import org.identityconnectors.framework.common.objects.AttributeInfo;
import org.identityconnectors.framework.common.objects.AttributeUtil;
import org.identityconnectors.framework.common.objects.Name;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.OperationOptions;
import org.identityconnectors.framework.common.objects.OperationalAttributeInfos;
import org.identityconnectors.framework.common.objects.OperationalAttributes;
import org.identityconnectors.framework.common.objects.Schema;
import org.identityconnectors.framework.common.objects.Uid;
import org.identityconnectors.sap.Function;
import org.identityconnectors.sap.SAPConfiguration;
import org.identityconnectors.sap.SAPConnection;
import org.identityconnectors.sap.SAPConnector;
import org.identityconnectors.sap.SAPConstants;
import org.identityconnectors.sap.SAPCreateUpdateBase;
import org.identityconnectors.sap.SAPUtil;

import com.sap.conn.jco.JCoException;
import com.sap.conn.jco.JCoStructure;
import com.sap.conn.jco.JCoTable;

/**
 * Implementor of the create operation for SAP
 * 
 * @author bfarrell
 * @version 1.0
 */
public class Create extends SAPCreateUpdateBase {
	private static final Log log = Log.getLog(Create.class);

	private Schema _schema;
	private SAPConnection _connection;
	private SAPConfiguration _configuration;
	private Map<String, AttributeInfo> _accountAttributes;
	private Set<String> _filteredAccounts = new HashSet<String>();
	public Map<String, String> _tableFormats = new HashMap<String, String>();

	/**
	 * Constructor
	 * 
	 * @param configuration
	 *            {@link org.identityconnectors.sap.SAPConfiguration} to be used
	 * @param connection
	 *            {@link org.identityconnectors.sap.SAPConnection} to be used
	 */
	public Create(SAPConnection conn, SAPConfiguration config,
			Map<String, AttributeInfo> accountAttributes,
			Set<String> filteredAccounts, Map<String, String> tableFormats) {
		super(conn, config, accountAttributes, filteredAccounts, tableFormats);
		_connection = conn;
		_configuration = config;
		_accountAttributes = accountAttributes;
		_tableFormats = tableFormats;
		_filteredAccounts = filteredAccounts;
	}

	/**
	 * Execute the actual create command.
	 * 
	 * @param objClass
	 * @param attrs
	 * @param options
	 * @return
	 */
	// TODO: break up method
	public Uid execute(final ObjectClass objClass, final Set<Attribute> attrs,
			final OperationOptions options) {
		log.info("BEGIN");
		if (objClass != null && !objClass.equals(ObjectClass.ACCOUNT))
			throw new IllegalArgumentException(_configuration.getMessage(
					"UNSUPPORTED_OBJECT_CLASS", objClass.getObjectClassValue()));
		boolean expirePassword = true;
		//boolean changePasswordAtNextLogon = _configuration.ischangePasswordAtNextLogon();
		boolean changePasswordAtNextLogon = false;
		//Added for Bug 19594110
		boolean isUserCreatedInTarget=false;
		Name name = AttributeUtil.getNameFromAttributes(attrs);
		String accountId = name.getNameValue();
		log.info(_configuration.getMessage("SAP_INFO_CREATE_ACCID", accountId));
		

		if (_filteredAccounts.contains(accountId)) {
			String message = _configuration.getMessage(
					"SAP_ERR_CREATE_FILTERED_ACCT", accountId);
			log.error(message);
			throw new ConnectorException(message);
		}

		// Check to see if the user exists
		boolean exists = checkIfUserExists(accountId);
		if (!exists) {
			try {
				Function function = getCorrectCreateFunction(accountId);
				// ObjectClassInfo account =
				// _schema.findObjectClassInfo(ObjectClass.ACCOUNT_NAME);
				Map<String, Attribute> attrMap = new HashMap<String, Attribute>(
						AttributeUtil.toMap(attrs));
				List<Object> masterSystem = new ArrayList<Object>();
				//Added for Bug 19594110
				Set<Attribute> childDataSet = new HashSet<Attribute>();
				List<Object> subSystems = null;
				String sNewPassword = null;
				final char [] _array = new char[50];
				String personnelNumber = null;
				boolean disableUser = false;
				Set<String> attrsSet = new HashSet<String>();
				for (Attribute attr : attrs) {
					String key = attr.getName();
					attrsSet.add(key);
					// Name.NAME = __NAME__
					if (Name.NAME.equals(key)) {
						function.setUserField((String) attr.getValue().get(0));
					} else if (SAPConstants.ATTR_PASSWORD.equals(key)) {
						//sNewPassword =  SAPUtil.decode((GuardedString)attr.getValue().get(0));
						((GuardedString)attr.getValue().get(0)).access(new GuardedString.Accessor() {
							public void access(char[] clearChars) {
								try {
									System.arraycopy(clearChars, 0, _array, 0, clearChars.length);
								} catch (Exception sException) {
									log.error(sException.getMessage());
								}
							}		
						});
						sNewPassword = new String(_array).trim();
					}

					// OperationalAttributes.OPERATIONAL_ATTRIBUTE_NAMES
					// (java.util.Collections$UnmodifiableSet<E>)
					// [__DISABLE_DATE__, __PASSWORD_EXPIRATION_DATE__,
					// __CURRENT_PASSWORD__, __PASSWORD__, __ENABLE_DATE__,
					// __PASSWORD_EXPIRED__, __ENABLE__, __LOCK_OUT__]

					// OperationalAttributes.CURRENT_PASSWORD_NAME =
					// __CURRENT_PASSWORD__
					// Uid.NAME=__UID__
					else if (SAPConstants.ATTR_ACCOUNT.equals(key)
							|| SAPConstants.LOGONDATA_LTIME.equals(key)
							|| OperationalAttributes.CURRENT_PASSWORD_NAME
									.equals(key) || Uid.NAME.equals(key)) {
					} else if (SAPConstants.PERSONNEL_NUMBER.equals(key)) {
						personnelNumber = (String) attr.getValue().get(0);
					} else if (SAPConstants.ATTR_CUA_SYSTEMS.equals(key)) {
						subSystems = attr.getValue();
					//Updated for Bug 19594110
					} else if (SAPConstants.ATTR_PROFILES.equals(key)) {
						//profiles = attr.getValue();
						childDataSet.add(AttributeBuilder.build(
								SAPConstants.ATTR_PROFILES_EMBEDED, attr.getValue()));
					} else if (key.equals(SAPConstants.ATTR_ROLES_EMBEDED) ||
							key.equals(SAPConstants.ATTR_PROFILES_EMBEDED) ||
							key.equals(SAPConstants.ATTR_PARAMETERS_EMBEDED) || 
							key.equals(SAPConstants.ATTR_GROUPS_EMBEDED)) {
						childDataSet.add(attr);
					} else if (OperationalAttributes.OPERATIONAL_ATTRIBUTE_NAMES
							.contains(key)
							|| SAPConnector._specialAttributes.contains(key)) {
						if (OperationalAttributes.ENABLE_NAME.equals(key)) {
							disableUser = !AttributeUtil.getBooleanValue(attr);
						} else if (OperationalAttributes.PASSWORD_EXPIRED_NAME
								.equals(key)) {
							expirePassword = AttributeUtil
									.getBooleanValue(attr);
						}

					} else {
						// if we dont want to eat exception and its a defined
						// attr (ie not null, so we know createable status)
						// and it is not creatable
						if (!_configuration.isEatNonUpdateCreate()
								&& _accountAttributes.get(key) != null
								&& !_accountAttributes.get(key).isCreateable()) {
							String message = _configuration.getMessage(
									"SAP_ERR_ATTR_NOT_CRT", attr.getName());
							log.error(message);
							ConnectorException ce = new ConnectorException(
									message);
							throw ce;
						}

						Object value = attr.getValue();

						// for single value attributes converting list into
						// single value ie from [value] to "value"
						if (_accountAttributes.get(key) == null) {
							List<Object> valList = attr.getValue();
							if (valList != null && valList.size() == 1) {
								if(key.startsWith("USERGROUP"))
								{
									value = attr.getValue();
								}
								else{
								Object obj = valList.get(0);
								value = obj != null ? obj.toString() : null;
								}
							}
						}

						// For Valid Through and Valid from attributes of User
						if (key.contains("GLTGB") || key.contains("GLTGV")) {
							value = attr.getValue().get(0);
							if (!value.toString().equalsIgnoreCase("0")) {
								if(_configuration.getmasterSystem()!=null){
									value = SAPUtil.longToDate(value.toString());
								} else {
									value = SAPUtil.stringToDate(value.toString(),"EEE MMM dd HH:mm:ss z yyyy");
								}
							} 
						}

						else if (_accountAttributes.get(attr.getName()) != null
								&& !_accountAttributes.get(attr.getName())
										.isMultiValued()) {
							value = AttributeUtil.getAsStringValue(attr);
						}

						// We have to break apart the attribute name because
						// it contains the structure or table name and the
						// attribute name
						String[] split = key.split(SAPConnector.DELIMITER);
						
						if(split.length > 4){
							addCustomAttribute(function, attr, accountId);
						} else {
							if (split.length != 4) {
								String message = _configuration.getMessage(
										"SAP_ERR_ACCT_ATTR_FORMAT", key);
								log.error(message);
								ConnectorException ce = new ConnectorException(
										message);
								throw ce;
							}
							String attributeName = split[0];
							String structOrTable = split[1];
							// Add it
							log.info(_configuration.getMessage("SAP_INFO_SET_ATTR", attributeName+"_"+value));
							function.setImportValue(structOrTable, attributeName,
									value, exists, _tableFormats);
						}	
					}
				}

				/*
				 * for (AttributeInfo attrInfo : account.getAttributeInfo()) {
				 * String key = attrInfo.getName(); // is it required attribute?
				 * if(attrInfo.isRequired() && !attrsSet.contains(key)) { String
				 * errMsg =
				 * _configuration.getMessage("SAP_ERR_MISSING_REQD_ATTR", key);
				 * log.error(errMsg); ConnectorException ce = new
				 * ConnectorException(errMsg); throw ce; } }
				 */

				String passwd = null;
				// Actual Password entered in user form
				String userPassword = getPassword(attrMap,
						OperationalAttributes.PASSWORD_NAME);
				
				
				if (!expirePassword) {
					// If the password needs to be unexpired,
					// SUSR_USER_CHANGE_PASSWORD_RFC must be
					// used to set the LTIME field (The
					// correction for SAP Note 750390 prevents
					// direct updating of the LOGONDATA->LTIME
					// field. The only way to do this is to use
					// SUSR_USER_CHANGE_PASSWORD_RFC. This BAPI
					// requires that the password be expired before
					// making the call when executed as an SAP admin.)
					// check the new password against SAP policy to prevent
					// setting the password if it fails SAP password policy
					validatePassword(new GuardedString(userPassword.toCharArray()));
					passwd = generateTempPassword();
				} else {
					passwd = userPassword;
				}

				if(_configuration.getchangePasswordAtNextLogon() != null && _configuration.getchangePasswordAtNextLogon().equalsIgnoreCase("NO")){
					/*GuardedString sDummyPwd = _configuration.getdummyPassword();
					if (sDummyPwd != null) {
						GuardedStringAccessor accessor = new GuardedStringAccessor();
						sDummyPwd.access(accessor);
						passwd = new String(accessor.getArray());
						accessor.clear();
					}*/
					final char[] _array1 = new char[50];
					_configuration.getdummyPassword().access(new GuardedString.Accessor() {
						public void access(char[] clearChars) {
							try {
								System.arraycopy(clearChars, 0, _array1, 0, clearChars.length);
							} catch (Exception sException) {
								log.error(sException.getMessage());
							}
						}
					});
					passwd = new String(_array1).trim();
					
				} 
				// Set the password
				function.setImportValue(SAPConnector.USERPASS, "BAPIPWD",
						new GuardedString(passwd.toCharArray()), false);
				
				function.executeWithRetry(_configuration.getMaxBAPIRetries());
				function.jcoErrorCheck();
				//Added for Bug 19594110
				isUserCreatedInTarget = true;
				// user was successfully created, now we can assign
				// in OIM IT resource contains a field named 'masterSystemName'
				// and this attribute is not there in OW.So this loop will be
				// executed only from oIM

				if (_configuration.getEnableCUA() && _configuration.getmasterSystem()!=null) {
					masterSystem.add(_configuration.getmasterSystem());
					addMasterSystem(accountId, masterSystem);
				} else {
					maintainSapSubSystems(accountId, subSystems);
				}
				
				//Added for Bug 19594110
				if(childDataSet.size()>0){
					Update update = new Update( _connection,_configuration,_accountAttributes,
							_filteredAccounts,_tableFormats);
					update.processChildTableData(new Uid(name.getNameValue()), childDataSet, false);
				}
				
				/*
				 * maintainSapProfiles(accountId, profiles);
				 * maintainSapActivityGroups(accountId, activityGroups);
				 */

				// Change password in SAP
				if (!expirePassword || changePasswordAtNextLogon) {
					// set last login time to unexpire the password
					// This can't be done on the create. Evidently,
					// BAPI_USER_CREATE1 sets LTIME itself, so we
					// have to set it outside of BAPI_USER_CREATE1
					unexpirePassword(accountId, passwd, userPassword);
				}

			
				if (_configuration.getmasterSystem() != null && _configuration.getchangePasswordAtNextLogon().equalsIgnoreCase("NO")) {
						changePassword("SUSR_USER_CHANGE_PASSWORD_RFC", accountId, new GuardedString(passwd.toCharArray()), new GuardedString(sNewPassword.toCharArray()));
				} 

				if (attrMap.containsKey(SAPConstants.ACCOUNT_LOCKED)) {
					if (attrMap.get(SAPConstants.ACCOUNT_LOCKED).getValue()
							.get(0).equals("1")) {
						modifyLockUnlockUser(accountId, true);
					} else {
						modifyLockUnlockUser(accountId, false);
					}
				}
				//START BUG 25344830 AOB: SAP USER RECON IS NOT WORKING WITH APPLICATION TEMPLATE 
				else if (attrMap.containsKey(OperationalAttributeInfos.LOCK_OUT.getName())) {
					log.info(" LOCK_OUT__ {0} ",attrMap.get(OperationalAttributeInfos.LOCK_OUT.getName()));
					if (AttributeUtil.getBooleanValue(attrMap.get(OperationalAttributeInfos.LOCK_OUT.getName()))) {
						modifyLockUnlockUser(accountId, true);
					} else {
						modifyLockUnlockUser(accountId, false);
					}
				}
				//END BUG 25344830 AOB: SAP USER RECON IS NOT WORKING WITH APPLICATION TEMPLATE 
				if (disableUser) {
					  disableUser(accountId,attrMap
								.get(OperationalAttributes.ENABLE_NAME));
			    }
				log.info("RETURN");
				return new Uid(accountId);
			} // end of try
			catch (Exception e) {//Updated for Bug 19594110
				String message = _configuration.getMessage(
						"SAP_ERR_CREATE_USER", accountId)+e.getMessage();
				log.error(e, message);
				try {
					//Updated for Bug 19594110
					if (isUserCreatedInTarget){
						Function function = new Function("BAPI_USER_DELETE", accountId, 
								_connection, _configuration.getConnectorMessages(),
		                        _configuration.getEnableCUA(), _configuration.getRetryWaitTime());
						function.executeWithRetry(_configuration.getMaxBAPIRetries());
						function.jcoErrorCheck();
						boolean throwNotExists = options != null && options.getOptions() != null && options.getOptions().get("throwNotExists") != null ? 
												(Boolean) options.getOptions().get("throwNotExists") : true;
						if (throwNotExists) {
							function.jcoUserNotExistCheck();
						}
					}
				}catch (JCoException ex) {
		            String message1 = _configuration.getMessage("SAP_ERR_DELETE_USER", accountId)+ex.getMessage();
		            log.error(message1);
		            ConnectorException ce = new ConnectorException(message1, ex);
		            throw ce;
		        }
				ConnectorException ce = new ConnectorException(message, e);
				throw ce;
			}
		} // end of if
		// user didn't exist
		else {
			String message = _configuration.getMessage(
					"SAP_ERR_CREATE_ACCT_DUPE", accountId);
			log.error(message);
			AlreadyExistsException aee = new AlreadyExistsException(message);
			throw aee;
		}
	}
}
