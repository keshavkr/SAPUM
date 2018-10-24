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

import java.util.ArrayList;
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
import org.identityconnectors.framework.common.objects.AttributeInfo;
import org.identityconnectors.framework.common.objects.AttributeUtil;
import org.identityconnectors.framework.common.objects.EmbeddedObject;
import org.identityconnectors.framework.common.objects.Name;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.OperationOptions;
import org.identityconnectors.framework.common.objects.OperationalAttributeInfos;
import org.identityconnectors.framework.common.objects.OperationalAttributes;
import org.identityconnectors.framework.common.objects.Uid;

import com.sap.conn.jco.JCoException;
import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.JCoFunctionTemplate;
import com.sap.conn.jco.JCoParameterList;
import com.sap.conn.jco.JCoRecord;
import com.sap.conn.jco.JCoRepository;
import com.sap.conn.jco.JCoStructure;
import com.sap.conn.jco.JCoTable;

/**
 * Implementor of the update operation for SAP
 * 
 * @author bfarrell
 * @version 1.0
 */
public class Update extends SAPCreateUpdateBase {
	private static final Log log = Log.getLog(Update.class);

	public Update(SAPConnection conn, SAPConfiguration config,
			Map<String, AttributeInfo> accountAttributes,
			Set<String> filteredAccounts, Map<String, String> tableFormats) {
		super(conn, config, accountAttributes, filteredAccounts, tableFormats);
	}

	/**
	 * Execute the actual delete update.
	 * 
	 * @param objClass
	 * @param attrs
	 * @param options
	 * @return
	 */
	public Uid execute(final ObjectClass objClass, final Set<Attribute> attrs,
			final OperationOptions options) {
		log.info("BEGIN");
		if (objClass != null && !objClass.equals(ObjectClass.ACCOUNT))
			throw new IllegalArgumentException(_configuration.getMessage(
					"UNSUPPORTED_OBJECT_CLASS", objClass.getObjectClassValue()));
		
		Name name = AttributeUtil.getNameFromAttributes(attrs);
		Uid uid = AttributeUtil.getUidAttribute(attrs);
		String accountId = null;
		if (uid == null) {
			accountId = name.getNameValue();
		} else {
			accountId = uid.getUidValue();
		}
		log.info(_configuration.getMessage("SAP_INFO_UPDATE_USR", accountId));

		// Update account.

		// update method needs to modify the individual account
		// attributes and not replace the entire record. The user password will
		// only be present if it is needing to be changed. The password if it
		// does need to be changed will not be found in the user.password
		// variable passed in, it will instead be found on the resource info
		// object password

		if (_filteredAccounts.contains(accountId)) {
			String message = _configuration.getMessage(
					"SAP_ERR_CHANGE_FILTERED_ACCT", accountId);
			log.error(message);
			throw new ConnectorException(message);
		}

		boolean expirePassword = false;
		boolean resetFailedLoginCnt = false;
		// hasCurrentPassword is used to signal that current password
		// has been sent and therefore a different BAPI should be used
		// to change the user's password
		boolean hasCurrentPassword = false;
		String personnelNumber = null;
		HashMap<String, ArrayList<SAPUMAttributeMapBean>> attrMapOIM = new HashMap<String, ArrayList<SAPUMAttributeMapBean>>();
		// Check to see if the user exists
		if (checkIfUserExists(accountId)) {
			try {
				Map<String, Attribute> attrMap = new HashMap<String, Attribute>(
						AttributeUtil.toMap(attrs));

				// handle enable/disable
				if (attrMap.containsKey(OperationalAttributes.ENABLE_NAME)) {
					disableUser(accountId, attrMap
							.get(OperationalAttributes.ENABLE_NAME));
				}
				// /Lock/Unlock user(Using BAPI_USER_LOCK/BAPI_USER_UNLOCK)
				//modified for contact test
				else if (attrMap.containsKey(SAPConstants.ACCOUNT_LOCKED)) {
					log.info("User Lock;NONE;NONE;NONE {0} ",attrMap.get(SAPConstants.ACCOUNT_LOCKED).getValue().get(0));
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
				else {
					Function function = new Function("BAPI_USER_CHANGE",
							accountId, _connection, _configuration
									.getConnectorMessages(), _configuration
									.getEnableCUA(), _configuration
									.getRetryWaitTime());

					List<Object> subSystems = null;
					//List<Object> profiles = null;
					//List<Object> activityGroups = null;
					//EmbeddedObject roleEmbeddedObj = null;
					//String sNewPassword = null;
					final char[] _array = new char[50];
					Set<Attribute> childDataSet = new HashSet<Attribute>();
					for (Attribute attr : attrs) {
						String key = attr.getName();

						AttributeInfo attrInfo = null;
						if(_accountAttributes != null)
							attrInfo = _accountAttributes.get(key);
						// if attrinfo is null we have to assume that it is
						// created
						// outside of the connector. therefor we have to assume
						// that it
						// is updateable
						// Added for Bug 19594110
						if (SAPConstants.ATTR_ROLES_EMBEDED.equals(key) || 
								SAPConstants.ATTR_PROFILES_EMBEDED.equals(key) || 
								SAPConstants.ATTR_PARAMETERS_EMBEDED.equals(key) || 
								SAPConstants.ATTR_GROUPS_EMBEDED.equals(key)) {
							//activityGroups = attr.getValue();
							//addMultiValueData(activityGroups, key, accountId);
							childDataSet.add(attr);
						}
						
						String[] split = key.split(SAPConnector.DELIMITER);
						if(split.length > 4){
							addCustomAttribute(function, attr, accountId);
						}
						
						if (attrInfo != null && !attrInfo.isUpdateable()) {
							String message = _configuration.getMessage(
									"SAP_ERR_ATTR_NOT_UPD", attr.getName());
							log.error(message);
							ConnectorException ce = new ConnectorException(
									message);
							throw ce;
						}

						if (SAPConstants.ATTR_ACCOUNT.equals(key)
								|| SAPConstants.LOGONDATA_LTIME.equals(key)
								|| OperationalAttributes.ENABLE_NAME
										.equals(key)
								|| OperationalAttributes.PASSWORD_NAME
										.equals(key) || Uid.NAME.equals(key)
								|| Name.NAME.equals(key)) {
							// skip it
						} else if (SAPConstants.PERSONNEL_NUMBER.equals(key)) {
							personnelNumber = (String) attr.getValue().get(0);
						} else if (SAPConstants.ATTR_CUA_SYSTEMS.equals(key)) {
							subSystems = attr.getValue() == null ? new ArrayList<Object>()
									: attr.getValue();
						} else if (SAPConstants.ATTR_PROFILES.equals(key)) {
							/*profiles = attr.getValue() == null ? new ArrayList<Object>()
									: attr.getValue();*/
						} else if (SAPConstants.ATTR_ACTIVITY_GROUPS
								.equals(key)) {
							/*activityGroups = attr.getValue() == null ? new ArrayList<Object>()
									: attr.getValue();*/
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
							//sNewPassword = new String(_array).trim();
							
						} else if (OperationalAttributes.CURRENT_PASSWORD_NAME
								.equals(key)) {
							// This attribute is used to specify the user's
							// current
							// password. It is needed if the user wants to
							// retain password
							// history and other password data (like
							// "Password Last Changed Date").
							hasCurrentPassword = attr.getValue() != null;
						} else if (OperationalAttributes.LOCK_OUT_NAME
								.equals(key)) {
							resetFailedLoginCnt = !AttributeUtil
									.getBooleanValue(attr)
									|| resetFailedLoginCnt;
						} else if (OperationalAttributes.PASSWORD_EXPIRED_NAME
								.equals(key)) {
							expirePassword = AttributeUtil
									.getBooleanValue(attr);
						} else if (!SAPConnector._specialAttributes
								.contains(key)) {
							handleNormalAttr(function, attr);
						} else if (SAPConstants.ATTR_ACCOUNT_LOCKED_WRNG_PWD
								.equals(key)) {
							// check for AA_LOCKED_WRNGLOGON_MAPNAME.
							// This attribute is different from the
							// BAPI_USER_UNLOCK
							// that is handled by the enable/disable methods.
							// See
							// unlockAccount() method for details.
							// This value is false unless it is explicitly set
							// to true
							// by this attribute.
							if (null != attr.getValue())
								resetFailedLoginCnt = !AttributeUtil
										.getBooleanValue(attr)
										|| resetFailedLoginCnt;
						} else if (SAPConstants.ACCOUNT_LOCKED.equals(key)) {
							// check for AA_LOCKED_WRNGLOGON_MAPNAME.
							// This attribute is different from the
							// BAPI_USER_UNLOCK
							// that is handled by the enable/disable methods.
							// See
							// unlockAccount() method for details.
							// This value is false unless it is explicitly set
							// to true
							// by this attribute.
						}
					}
					// Added for Bug 19594110
					if(childDataSet.size()>0){
						processChildTableData(uid, childDataSet, false);
					}
					
					String userPassword = getPassword(attrMap,
							OperationalAttributes.PASSWORD_NAME);
					String passwd = null;
					boolean passwdSet = false;
					boolean doRename = name != null
							&& uid != null
							&& !name.getNameValue().equalsIgnoreCase(
									uid.getUidValue());
					if (null != userPassword && !hasCurrentPassword) {
						// Try to set the password if the user's current
						// password
						// is not provided. If it is, use a different SAP API to
						// set the password in order to check password history
						// and
						// set other related password data.
						if (expirePassword) {
							passwd = userPassword;
						} else {
							// If the password needs to be unexpired,
							// SUSR_USER_CHANGE_PASSWORD_RFC must be
							// used to set the LTIME field (The
							// correction for SAP Note 750390 prevents
							// direct updating of the LOGONDATA->LTIME
							// field. The only way to do this is to use
							// SUSR_USER_CHANGE_PASSWORD_RFC. This BAPI
							// only allows the password to be changed only once
							// per day if the password is not expired, so it
							// requires that the password be expired before
							// making the call when executed as an SAP admin.)

							// See if the password is already set to the value
							// of userPassword
							if (_configuration.getmasterSystem() == null && !(passwdSet = isPasswordAlreadySet(accountId,
									userPassword))) {
								// check the new password against SAP policy to
								// prevent
								// setting the password if it fails SAP password
								// policy
								validatePassword(new GuardedString(userPassword.toCharArray()));
								passwd = generateTempPassword();
							}
						}
						
						if(_configuration.getchangePasswordAtNextLogon() != null && _configuration.getchangePasswordAtNextLogon().equalsIgnoreCase("NO")){
							GuardedString sDummyPwd = _configuration.getdummyPassword();
							final char[] _array1 = new char[50];
							if (sDummyPwd != null) {
								sDummyPwd.access(new GuardedString.Accessor() {
									public void access(char[] clearChars) {
										try {
									        System.arraycopy(clearChars, 0, _array1, 0, clearChars.length); 
										} catch (Exception sException) {
											log.error(sException.getMessage());
										}
				                	}
								});
								passwd = new String(_array1).trim();
								/*GuardedStringAccessor accessor = new GuardedStringAccessor();
								sDummyPwd.access(accessor);
								passwd = new String(accessor.getArray());
								accessor.clear();*/
							}
						}

						// dont do a change if we are going to rename
						// do it after for the new user
						if (_configuration.getmasterSystem() == null && !doRename && !passwdSet) {
							// Set the password
							function.setImportValue(SAPConnector.USERPASS,
									"BAPIPWD", new GuardedString(passwd.toCharArray()), true);
						} else {
							if(_configuration.getpasswordPropagateToChildSystem().equalsIgnoreCase("NO") && _configuration.getchangePasswordAtNextLogon().equalsIgnoreCase("no")){
								function.setImportValue(SAPConnector.USERPASS,
										"BAPIPWD", new GuardedString(passwd.toCharArray()), true);
							}
						}
					}

					// if not CUA and we want the password expired even if the
					// user supplied the current password
					// just do it now to avoid doing it again
					if (hasCurrentPassword && expirePassword) {
						function.setImportValue(SAPConnector.USERPASS,
								"BAPIPWD", new GuardedString(userPassword.toCharArray()), true);
					}
					//log.error("Perf: Updating attributes "+AttributeUtil.getBasicAttributes(attrs)+" for user {0} ",accountId);
					function.executeWithRetry(_configuration
							.getMaxBAPIRetries());
					//log.error("Perf: Update completed for attributes "+AttributeUtil.getBasicAttributes(attrs)+" for user {0} ",accountId);					
					function.jcoErrorCheck();

					// profile/group assignments must be done 1st for CUA,
					// and doesn't matter on non-CUA systems
					maintainSapSubSystems(accountId, subSystems);
					/*maintainSapProfiles(accountId, profiles);
					maintainSapActivityGroups(accountId, activityGroups);*/

					// rename user before we start messing with passwords
					if (doRename) {
						accountId = handleRename(function, new GuardedString(passwd.toCharArray()), uid, name,
								attrMap);
					}

					if (resetFailedLoginCnt) {
						// unlock the user's account
						resetFailedLoginCount(accountId);
					}

					if (hasCurrentPassword) {
						changeUserPassword(accountId, AttributeUtil
								.toMap(attrs), expirePassword);
					} else if (userPassword != null && !passwdSet && _configuration.getmasterSystem() == null) {
						// in all cases that we have set a new password we must
						// make sure it gets propagated to the child systems
						childPasswordInit(accountId);
						if (!expirePassword) {
							// SAP Note 750390 is installed
							// We've already checked the new password for
							// validity against the SAP system policy
							unexpirePassword(accountId, passwd, userPassword);
						}
					}
					
					if (_configuration.getmasterSystem() != null && userPassword != null){
						Function function1 = null;
						if(_configuration.getchangePasswordAtNextLogon().equalsIgnoreCase("YES")){
							 function1 = new Function("BAPI_USER_CHANGE", accountId, _connection, _configuration
									.getConnectorMessages(), _configuration.getEnableCUA(), _configuration.getRetryWaitTime());
									
							if(_configuration.getpasswordPropagateToChildSystem().equalsIgnoreCase("NO")){
								function1.setImportValue(SAPConnector.USERPASS,
										"BAPIPWD", new GuardedString(userPassword.toCharArray()), true);
							} else { 
								function1 = new Function(_configuration.getCuaChildInitialPasswordChangeFuncModule(), _connection, _configuration
										.getConnectorMessages(), _configuration.getEnableCUA(), _configuration.getRetryWaitTime());
								function1.setImportValue(null, "USERNAME", accountId, false);
								function1.setImportValue("PASSWORD", "BAPIPWD", new GuardedString(userPassword.toCharArray()), true);
							}
							function1.executeWithRetry(_configuration.getMaxBAPIRetries());
							function1.jcoErrorCheck();
						} else {
							
							if(_configuration.getpasswordPropagateToChildSystem().equalsIgnoreCase("NO")){
								changePassword("SUSR_USER_CHANGE_PASSWORD_RFC", accountId, new GuardedString(passwd.toCharArray()),new GuardedString(userPassword.toCharArray()));
						    } else {
						    	function1 = new Function(_configuration.getCuaChildInitialPasswordChangeFuncModule(), _connection, _configuration
										.getConnectorMessages(), _configuration.getEnableCUA(), _configuration.getRetryWaitTime());
								// Start:: Bug 23336145 - SAP UM: NW7.5SAP UM UPDATE PASSWORD NOT WORKING -Used the below variable in the custom BAPI's
						    	String accountIdUpper = accountId.toUpperCase();
						    	// End:: Bug 23336145 - SAP UM: NW7.5SAP UM UPDATE PASSWORD NOT WORKING
						    	function1.setImportValue(null, "USERNAME", accountIdUpper, false);
								function1.setImportValue("PASSWORD", "BAPIPWD", new GuardedString(passwd.toCharArray()), true);
								//function1.setImportValue(null, "PASSWORDX", "BAPIPWD", passwd, false);
								function1.executeWithRetry( _configuration.getMaxBAPIRetries());
								function1.jcoErrorCheck();
								
						    	function1 = new Function(_configuration.getCuaChildPasswordChangeFuncModule(), _connection, _configuration
										.getConnectorMessages(), _configuration.getEnableCUA(), _configuration.getRetryWaitTime());
								function1.setImportValue(null, "ZXLUSERNAME", accountIdUpper, false);
								function1.setImportValue(null, "ZXLOLD_PASSWORD", new GuardedString(passwd.toCharArray()), false);
								function1.setImportValue(null, "ZXLNEW_PASSWORD", new GuardedString(userPassword.toCharArray()), false);
								function1.executeWithRetry(_configuration.getMaxBAPIRetries());
								//function1.jcoErrorCheck();
						    	//changePassword(_configuration.getCuaChildPasswordChangeFuncModule(), accountId, passwd, userPassword);
						    }
						}
					}
				}
				
				if (personnelNumber != null) {
					createLink(accountId, personnelNumber);
				} 
				
			} catch (JCoException jcoe) {
				log.error(jcoe.getMessage());
				throw new ConnectorException(jcoe);
			} catch (ConnectorException ce) {
				throw ce;
			} catch (java.lang.Exception e) {
				String message = _configuration.getMessage(
						"SAP_ERR_UPDATE_USER", accountId);
				log.error(message);
				throw new ConnectorException(message, e);
			}
		} // end of user check if
		else {
			String message = _configuration.getMessage(
					"SAP_ERR_ACCT_EXIST_UPDATE", accountId);
			log.error(message);
			throw new ConnectorException(message);
		}
		log.info("RETURN");
		return new Uid(accountId);
	}

	/**
	 * Handle the rename and password setting for the new user.
	 * 
	 * @param function
	 * @param passwd
	 * @param uid
	 * @param name
	 * @param attrMap
	 * @return
	 * @throws JCoException
	 */
	private String handleRename(Function function, GuardedString passwd, Uid uid,
			Name name, Map<String, Attribute> attrMap) throws JCoException {
		// Rename operation
		log.info("BEGIN");
		String accountId = renameUser(uid.getUidValue(), name.getNameValue()
				.toUpperCase());

		// if we didnt set the password by now, it is because we have the
		// current password
		// we still need to set it for the rename to the current
		if (passwd == null) {
			passwd = new GuardedString((getPassword(attrMap,
					OperationalAttributes.CURRENT_PASSWORD_NAME)).toCharArray());
		}

		// bug#22666: set password only if we have it
		if (passwd != null) {
			function = new Function("BAPI_USER_CHANGE", accountId, _connection,
					_configuration.getConnectorMessages(), _configuration
							.getEnableCUA(), _configuration.getRetryWaitTime());

			function.setImportValue(SAPConnector.USERPASS, "BAPIPWD",passwd,
					true);
			function.executeWithRetry(_configuration.getMaxBAPIRetries());
			function.jcoErrorCheck();
		}
		log.info("RETURN");
		return accountId;
	}

	/**
	 * Handle the normal attribute.
	 * 
	 * @param function
	 * @param attr
	 * @param key
	 * @throws JCoException
	 */
	private void handleNormalAttr(Function function, Attribute attr)
			throws JCoException {
		log.info("BEGIN");
		String key = attr.getName();
		Object value = attr.getValue();

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

		else if (_accountAttributes.get(key) != null
				&& !_accountAttributes.get(key).isMultiValued()) {
			value = AttributeUtil.getAsStringValue(attr);
		}

		// once these attrs are set, they cannot be nulled out
		if ((key.equals(SAPConstants.ATTR_LANGUAGE_KEY_P) || key
				.equals(SAPConstants.ATTR_USER_TYPE))
				&& value == null) {
			String message = _configuration.getMessage("SAP_ERR_ATTR_INVLD",
					null, key);
			log.error(message);
			throw new IllegalArgumentException(message);
		}

		// We have to break apart the attribute name because
		// it contains the structure or table name and the
		// attribute name
		String[] split = key.split(SAPConnector.DELIMITER);
		if (split.length != 4) {
			String message = _configuration.getMessage(
					"SAP_ERR_ACCT_ATTR_FORMAT", key);
			log.error(message);
			ConnectorException ce = new ConnectorException(message);
			throw ce;
		}
		String attributeName = split[0];
		String structOrTable = split[1];
		// Add it
		log.info(_configuration.getMessage("SAP_INFO_SET_ATTR",attributeName + " - "+value));
		function.setImportValue(structOrTable, attributeName, value, true,
				_tableFormats);
		log.info("RETURN");
	}

	/**
	 * Enable user on the SAP resource.
	 * 
	 * @param accountId
	 *            - account to enable
	 * @throws ConnectorException
	 */
	protected void enableUser(String accountId) throws ConnectorException {
		log.info("BEGIN");
		try {
			Function function = new Function("BAPI_USER_UNLOCK", accountId,
					_connection, _configuration.getConnectorMessages(),
					_configuration.getEnableCUA(), _configuration
							.getRetryWaitTime());
			function.executeWithRetry(_configuration.getMaxBAPIRetries());
			function.jcoErrorCheck();
			log.info("RETURN");
		} catch (JCoException ex) {
			String message = _configuration.getMessage("SAP_ERR_ENABLE_USER");
			log.error(ex, message);
			throw new ConnectorException(message, ex);
		}
	}

	/**
	 * Unlocks an SAP user's account if the account was locked because the
	 * number of failed login attempts was exceeded. This differs from
	 * enable/disable in that this is not an SAP administrative function. This
	 * type of lock only occurs when the number of failed login attempts is
	 * Note: SUSR_BAPI_USER_UNLOCK must be remote enabled for this to work.
	 * exceeded.
	 * 
	 * @param accountId
	 * @throws JCoException
	 */
	protected void resetFailedLoginCount(String accountId) throws JCoException {
		log.info("BEGIN");
		Function function = new Function("SUSR_BAPI_USER_UNLOCK", accountId
				.toUpperCase(), _connection, _configuration
				.getConnectorMessages(), _configuration.getEnableCUA(),
				_configuration.getRetryWaitTime());
		if (function.getImportParameterList().getListMetaData().isStructure(
				"LOCK_WRONG_LOGON")) {
			function.setImportValue("LOCK_WRONG_LOGON", "BAPIFLAG", "X", false);
		} else {
			function.setImportValue(null, "LOCK_WRONG_LOGON", "X", false);
		}

		function.executeWithRetry(_configuration.getMaxBAPIRetries());
		function.jcoErrorCheck();
		log.info("RETURN");
	}

	/**
	 * Sets the user's password with a different BAPI call so that the password
	 * history and other password data (like "Password Last Changed Date") is
	 * set correctly. Although this BAPI is executed as the admin, the concept
	 * is that the password is changed as the user.
	 * 
	 * @param accountId
	 * @param attrMap
	 * @param expirePassword
	 * @throws ConnectorException
	 * @throws JCoException
	 */
	protected void changeUserPassword(String accountId,
			Map<String, Attribute> attrMap, boolean expirePassword)
			throws ConnectorException, JCoException {
		log.info("BEGIN");
		String newPassword = getPassword(attrMap,
				OperationalAttributes.PASSWORD_NAME);
		String currentPassword = getPassword(attrMap,
				OperationalAttributes.CURRENT_PASSWORD_NAME);

		if (newPassword != null && currentPassword != null) {
			realPasswordChange(accountId, new GuardedString(currentPassword.toCharArray()), new GuardedString(newPassword.toCharArray()),
					!expirePassword);
		} else {
			newPassword = null;
			currentPassword = null;
			// The current password and the new password are required.
			String message = _configuration.getMessage("SAP_ERR_CHANGE_PASSWD");
			log.error(message);
			throw new ConnectorException(message);
		} // if..else newPassword && currentPassword
		newPassword = null;
		currentPassword = null;
		log.info("RETURN");
	}

	/**
	 * BUG: 23211442, This function tries to determine if the password has already been set.
	 * 
	 * @param identity
	 * @param password
	 * @return
	 * @throws JCoException
	 */
	protected boolean isPasswordAlreadySet(String accountId, String password)
			throws JCoException {
		log.info("BEGIN");
		// To determine if a user's password is expired,
		// call SUSR_LOGIN_CHECK_RFC with no password. If the password
		// is expired, an exception will be thrown with "PASSWORD_EXPIRED"
		// as the key and the message. (Got this tip from SAP development
		// support.) If the password is the wrong password, the error is
		// "WRONG_PASSWORD"
		boolean alreadySet = false;
		if (password != null && password.length() > 0) {
			// No error is returned if no password is specified, so make sure
			// that a password exists.
			alreadySet = true;
			try {
				Function function = new Function("SUSR_LOGIN_CHECK_RFC",
						_connection, _configuration.getConnectorMessages(),
						_configuration.getEnableCUA(), _configuration
								.getRetryWaitTime());
				function.setImportField("BNAME", accountId);
				function.setImportField("PASSWORD", password);
				function.executeWithRetry(_configuration.getMaxBAPIRetries());
			} catch (JCoException e) {
				log.error(e.getMessage());
				alreadySet = false;
			}
		}
		log.info("RETURN");
		return alreadySet;
	}

	/**
	 * Change the initial password on a child system.
	 * 
	 * @param accountId
	 * @throws JCoException
	 * @throws ConnectorException
	 */
	protected void childPasswordInit(String accountId) throws JCoException,
			ConnectorException {
		log.info("BEGIN");
		// change initial password on a child system
		// special case the CUA child password change. Initial passwords
		// are not replicated to child systems. The create of a user will
		// generate a special iDOC which contains the password after that the
		// iDOC will not be generated any more if the password is changed
		// unless you use the GUI. We can not call the specific BAPI from JCO
		// to emulate that behaviour. If we have CUA enabled and the RFC is set
		// in the config then execute this special BAPI which will cause the
		// iDOC's to be generated.
		// It relies on the presence of an initial password on the CUA master
		// which must be set via the BAPI_USER_CHANGE before calling this
		// method.
		String rfcNameSet = _configuration
				.getCuaChildInitialPasswordChangeFuncModule();
		if (_configuration.getEnableCUA() && rfcNameSet != null
				&& rfcNameSet.length() != 0) {
			log.info(_configuration.getMessage("SAP_INFO_SET_INIT_PWD_CUA_CHILD"));
			// get the list of children (no empty strings in the list)
			ArrayList<String> children = SAPUtil.getSubSystems(accountId, null,
					_connection, _configuration);
			if (children != null && children.size() > 0) {
				// keep track of a change failure try all systems before we
				// really
				// fail unless we have a FM issue, not a password change issue
				boolean failedOnce = false;
				ConnectorException ce = null;
				// walk over the list of assigned child systems
				for (String child : children) {
					if (child == null || child == "") {
						continue;
					}
					// create the function
					Function function = new Function(rfcNameSet, _connection,
							_configuration.getConnectorMessages(),
							_configuration.getEnableCUA(), _configuration
									.getRetryWaitTime());
					function.setImportValue(null, "BNAME", accountId, false);
					function.setImportValue(null, "CHILD_SYSTEM", child, false);
					// the custom function does not use a return table so we
					// should not
					// have to check for it. But the code is only provided as an
					// example consumers could change it and use a RETURN table
					// so lets just check for it as a precaution. If there is no
					// RETURN
					// table nothing will happen.
					try {
						function.executeWithRetry(_configuration
								.getMaxBAPIRetries());
					} catch (JCoException e) {
						String message;
						int group = e.getGroup();
						if (group != JCoException.JCO_ERROR_FUNCTION_NOT_FOUND) {
							message = _configuration.getMessage(
									"SAP_ERR_PASSWD_INITIAL_CHILD", child);
							failedOnce = true;
							ce = SAPUtil.addConnectorExceptionMessage(e,
									message);
							// remove this child from the underlying list so
							// we do not check it later for its state
							log.info(_configuration.getMessage("SAP_INFO_FAILED_INIT_PWD_CUA_CHILD"),child);
							
						} else {
							message = _configuration.getMessage(
									"SAP_ERR_JCO_PASSWD_FUNCTION", rfcNameSet);
							ConnectorException ce2 = new ConnectorException(
									message, e);
							log.error(message);
							throw ce2;
						}
					}
					function.jcoErrorCheck();
				}
				if (failedOnce) {
					throw ce;
				}
			}
		}
		log.info("RETURN");
	}

	/**
	 * Renames a user in SAP. Not supported if CUA is enabled.
	 * 
	 * @param accountId
	 *            - old account ID
	 * @param newAccountId
	 *            - new account ID
	 * @return new account ID
	 * @throws ConnectorException
	 * @throws JCoException
	 *             if a fatal JCoException occurs Note: The password is not set
	 *             on the new user. A separate change password operation is
	 *             required to set the password.
	 */
	// TODO: break this method up
	@SuppressWarnings("unchecked")
	protected String renameUser(String accountId, String newAccountId)
			throws ConnectorException, JCoException {
		log.ok(_configuration.getMessage("SAP_INFO_RENAME", accountId,
				newAccountId));

		// rename is not available when using CUA
		if (_configuration.getEnableCUA()) {
			String msg = _configuration.getMessage("SAP_ERR_RENAME_CUA");
			log.error(msg);
			ConnectorException ce = new ConnectorException(msg);
			throw ce;
		}

		// check if newIdentity already exists.
		if (checkIfUserExists(newAccountId)) {
			String message = _configuration.getMessage(
					"SAP_ERR_RENAME_USER_EXISTS", accountId, newAccountId);
			log.error(message);
			AlreadyExistsException aee = new AlreadyExistsException(message);
			throw aee;
		}

		// get the user
		log.ok(_configuration.getMessage("SAP_INFO_GET_USR", accountId));
		Function function = new Function("BAPI_USER_GET_DETAIL", accountId,
				_connection, _configuration.getConnectorMessages(),
				_configuration.getEnableCUA(), _configuration
						.getRetryWaitTime());
		function.executeWithRetry(_configuration.getMaxBAPIRetries());

		// get the alias field
		JCoStructure aliasStruct = function.getStructure("ALIAS");
		String identityAlias = aliasStruct.getString("USERALIAS");

		log.ok(_configuration.getMessage("SAP_INFO_CLONE_USR", accountId));
		JCoParameterList exportParams = function.getExportParameterList();
		JCoRecord exportClone = (JCoRecord) exportParams.clone();

		JCoParameterList exportTable = function.getTableParameterList();
		JCoRecord exportTableClone = (JCoRecord) exportTable.clone();

		// Create a new user
		log.ok(_configuration.getMessage("SAP_INFO_CREATE_RNM", newAccountId));
		Function createFunc = getCorrectCreateFunction(newAccountId);

		JCoParameterList importParams = createFunc.getImportParameterList();
		JCoParameterList importTables = createFunc.getTableParameterList();
		// BAPI_USER_CREATE1 does not provide input tables for Activity Groups
		// or Profiles, so these must be performed with separate APIs
		importParams.copyFrom(exportClone);
		importTables.copyFrom(exportTableClone);
		createFunc.setUserField(newAccountId);

		if (identityAlias != null && identityAlias.length() > 0) {
			// Can't set the alias to the alias of an existing user. Must set it
			// after the
			// old user is deleted. So, if an alias was set on the old user, set
			// this one
			// to empty and then set it after the old user was deleted.
			createFunc.setImportValue("ALIAS", "USERALIAS", "", false);
		}
		createFunc.executeWithRetry(_configuration.getMaxBAPIRetries());
		createFunc.jcoErrorCheck();

		try {
			// add the activity groups and profiles
			List ags = SAPUtil.getActivityGroups(accountId, function,
					_connection, _configuration, _tableFormats);
			maintainSapActivityGroups(newAccountId, ags);

			List profs = SAPUtil.getProfiles(accountId, function, _connection,
					_configuration, _tableFormats);
			maintainSapProfiles(newAccountId, profs);

			log.ok(_configuration.getMessage("SAP_INFO_GET_PDATA", accountId));
			// Copy the personalization data
			Function getPersFunc = new Function("SPERS_GET_DISTRIBUTION_DATA",
					_connection, _configuration.getConnectorMessages(),
					_configuration.getEnableCUA(), _configuration
							.getRetryWaitTime());
			getPersFunc.setImportField("P_USER", accountId.toUpperCase());
			getPersFunc.executeWithRetry(_configuration.getMaxBAPIRetries());

			JCoTable srcPersDataTable = getPersFunc
					.getTable("P_DISTRIBUTION_DATA");
			int srcPersDataRows = srcPersDataTable.getNumRows();
			if (srcPersDataRows > 0) {
				log.ok(_configuration.getMessage("SAP_INFO_COPY_PDATA",
						newAccountId));
				Function setPersFunc = new Function(
						"SPERS_SET_DISTRIBUTION_DATA", _connection,
						_configuration.getConnectorMessages(), _configuration
								.getEnableCUA(), _configuration
								.getRetryWaitTime());
				setPersFunc
						.setImportField("P_USER", newAccountId.toUpperCase());
				setPersFunc.getTableParameterList().setValue(
						"P_DISTRIBUTION_DATA", srcPersDataTable);
				setPersFunc
						.executeWithRetry(_configuration.getMaxBAPIRetries());
			}

			// set lock status correctly
			/*
			 * LockStatus lockStatus = new LockStatus(function); if
			 * (lockStatus.isDisabled()) { disableUser(newAccountId); }
			 */

		} catch (JCoException e) {
			// If one of the above operations fails, try to delete the user that
			// was just created.
			// This should never happen in normal circumstances, but could
			// happen.
			try {
				log.error(_configuration.getMessage("SAP_ERR_COPY_DATA",
						newAccountId));
				new Delete(_connection, _configuration, _filteredAccounts)
						.execute(ObjectClass.ACCOUNT, new Uid(newAccountId),
								null);
			} catch (ConnectorException ce) {
				String message = _configuration.getMessage(
						"SAP_WARN_RENAME_DELETE_NEW", accountId)
						+ ce.getLocalizedMessage();
				log.warn(message);
			}
			throw e;
		}

		// If an error occurs during the delete of the old user, log a warning
		// but still succeed the rename operation. In this case, the user
		// has been cloned (except for possibly the ALIAS field), but the old
		// user will
		// still exist.
		// If the user is deleted, but there is a problem setting the alias,
		// this is also
		// a warning.
		boolean userDeleted = false;
		try {
			new Delete(_connection, _configuration, _filteredAccounts).execute(
					ObjectClass.ACCOUNT, new Uid(accountId), null);
			userDeleted = true;
		} catch (ConnectorException ce) {
			String message = _configuration.getMessage(
					"SAP_WARN_RENAME_DELETE", accountId)
					+ ce.getLocalizedMessage();
			log.warn(message);
		}

		try {
			// Must set the alias if it was set on the original user
			if (userDeleted && identityAlias != null
					&& identityAlias.length() > 0) {
				log.ok(_configuration.getMessage("SAP_INFO_COPY_ALIAS"),accountId, newAccountId);
				log.ok(_configuration.getMessage("SAP_INFO_ALIAS"),identityAlias);
				
				Function changeFunc = new Function("BAPI_USER_CHANGE",
						newAccountId, _connection, _configuration
								.getConnectorMessages(), _configuration
								.getEnableCUA(), _configuration
								.getRetryWaitTime());
				changeFunc.setImportValue("ALIAS", "USERALIAS", identityAlias,
						true);
				changeFunc.executeWithRetry(_configuration.getMaxBAPIRetries());
				changeFunc.jcoErrorCheck();
			}
		} catch (JCoException e) {
			String message = _configuration.getMessage("SAP_WARN_RENAME_ALIAS",
					accountId)
					+ e.getLocalizedMessage();
			log.error(message);
		}
		return newAccountId;
	}
	
	
	
	private void createLink(String sUserId, String sEmplId)
			throws ConnectorException {
		log.info("BEGIN");
		boolean isLock = true;
		boolean isLink = true;
		try { 
			// TODO
			// Check the functionality with the released UM connector
			// Validate PERNR exists in ER
			// If it exists, then check if OverWrite link or not
			// If Overwrite link is true, then create link.
			_connection.getStartContext();
			boolean isOverwriteLink = _configuration.isOverwriteLink();
			boolean isValidatePernr = _configuration.isValidatePERNR();
			Date stDate = new Date();
			if (isValidatePernr) {
				isLink = validatePERNR(sEmplId);
				/*
				 * if (isExists) { isOverwriteLink =
				 * _configuration.isOverwriteLink(); }
				 */
			} else {
				log.error(_configuration.getMessage("SAP_ERR_PERSON_NUM_NOT_VAILDATED"));
				
			}

			if (isLink && !isOverwriteLink) {
				isLink = linkedBefore(sEmplId);
			} else if (isOverwriteLink) {
				log.error(_configuration.getMessage("SAP_ERR_OVERWRITE_LINK"));
			}

			if (!isLink && !isOverwriteLink) {

				log.error(_configuration.getMessage("SAP_ERR_PERSONNEL_NUM_ALREADY_LINKED"));
				
			}
			if (isOverwriteLink) {
				Function function1 = new Function("BAPI_EMPLOYEE_ENQUEUE",
						_connection, _configuration.getConnectorMessages(),
						false, _configuration.getRetryWaitTime());
				function1.setImportValue(null, "NUMBER", sEmplId, false);
				function1.executeWithRetry(_configuration.getMaxBAPIRetries());
				function1.jcoErrorCheck();
				JCoStructure returnStructure1 = function1
						.getStructure("RETURN");
				if (!returnStructure1.getString("NUMBER").equalsIgnoreCase(
						"000")) {
					isLock = false;
					String message = returnStructure1.getString("MESSAGE");
					log.error(message);
					ConnectorException ce = new ConnectorException(message);
					throw ce;
				}
				if (isLock) {
					// we execute a second BAPI to link a HR user to SAP user
					Function function2 = new Function("BAPI_EMPLCOMM_CREATE",
							_connection, _configuration.getConnectorMessages(),
							false, _configuration.getRetryWaitTime());
					// TODO
					// Check if the constants are to be taken from configuration
					Date dtCurrent = new Date();
					Date toDate = SAPUtil.stringToDate("31.12.9999",
							"dd.MM.yyyy");
					function2.setImportValue(null, "EMPLOYEENUMBER", sEmplId,
							false);
					function2.setImportValue(null, "SUBTYPE", "0001", false);
					function2.setImportValue(null, "VALIDITYBEGIN", dtCurrent,
							false);
					function2
							.setImportValue(null, "VALIDITYEND", toDate, false);
					function2.setImportValue(null, "COMMUNICATIONID", sUserId,
							false);
					
         			function2.executeWithRetry(_configuration.getMaxBAPIRetries());
					function2.jcoErrorCheck();
					
					JCoStructure returnStructure2 = function2
							.getStructure("RETURN");

					// if the employee is linked to a user, set the flag to true
					if (!returnStructure2.getString("NUMBER").equalsIgnoreCase("000")) {
						String message = returnStructure2.getString("MESSAGE");
						log.error(message);
						ConnectorException ce = new ConnectorException(message);
						throw ce;
					}
				} else {
					log.error(_configuration.getMessage("SAP_ERR_USR_LCK_FAILED"));
				}
				if (isLock) {
					Function function3 = new Function("BAPI_EMPLOYEE_DEQUEUE",
							_connection, _configuration.getConnectorMessages(),
							false, _configuration.getRetryWaitTime());
					function3.setImportValue(null, "NUMBER", sEmplId, false);
					function3.executeWithRetry(_configuration.getMaxBAPIRetries());
					function3.jcoErrorCheck();
					JCoStructure returnStructure3 = function3
							.getStructure("RETURN");
					if (!returnStructure3.getString("NUMBER").equalsIgnoreCase(
							"000")) {
						String message = returnStructure3.getString("MESSAGE");
						log.error(message);
						ConnectorException ce = new ConnectorException(message);
						throw ce;
					}
				} else {
					log.error(_configuration.getMessage("SAP_ERR_USR_UNLCK_FAILED"));
				}
			}
		} catch (JCoException jcoException) {
			throw new ConnectorException(jcoException);
		} catch (ConnectorException exception) {
			if (exception.getMessage().startsWith("Connection")) {
				throw new ConnectorException("Connection error occured",
						exception);
			}
			throw new ConnectorException(exception);
		} catch (Exception exception) {
			throw new ConnectorException(exception);
		} finally {
			_connection.getEndContext();
		}
		log.info("RETURN");
	}
	
	/**
	 * Description:Used to check if the personnel number exists on the target
	 * system by running BAPI_EMPLOYEE_CHECKEXISTENCE
	 * 
	 * @param empID
	 *            The Employee Number that links with the R/3 User. For example:
	 *            1000
	 * 
	 * @return boolean
	 * @throws ConnectorException
	 */
	private boolean validatePERNR(String empID) throws ConnectorException {
		log.info("BEGIN");
		boolean isExists = false;
		try {
			Function function3 = new Function("BAPI_EMPLOYEE_CHECKEXISTENCE",
					_connection, _configuration.getConnectorMessages(), false,
					_configuration.getRetryWaitTime());
			function3.setImportValue(null, "NUMBER", empID, false);
			function3.executeWithRetry(_configuration.getMaxBAPIRetries());
			function3.jcoErrorCheck();
			JCoStructure returnStructure3 = function3.getStructure("RETURN");
			String sTemp = returnStructure3.getString("CODE");
			if (StringUtil.isEmpty(sTemp)) {
				isExists = true;
			}
		} catch (JCoException exception) {
			throw new ConnectorException(exception);
		} catch (ConnectorException exception) {
			throw new ConnectorException(exception);
		} catch (Exception exception) {
			throw new ConnectorException(exception);
		}
		log.info("RETURN");
		return isExists;
	}
	
	/**
	 * Description:Used to validate if the personnel number is already linked
	 * before on the target system by running BAPI_EMPLCOMM_GETDETAILEDLIST
	 * 
	 * @param empID
	 *            The Employee Number that links with the R/3 User. For example:
	 *            1000
	 * 
	 * @return boolean
	 * @throws ConnectorException
	 */
	private boolean linkedBefore(String empID) throws ConnectorException {
		log.info("BEGIN");
		boolean isCheck = true;
		int iTemp;
		try {
			Function function3 = new Function("BAPI_EMPLCOMM_GETDETAILEDLIST",
					_connection, _configuration.getConnectorMessages(), false,
					_configuration.getRetryWaitTime());
			function3.setImportValue(null, "EMPLOYEENUMBER", empID, false);
			function3.setImportValue(null, "SUBTYPE", "0001", false);
			function3.executeWithRetry(_configuration.getMaxBAPIRetries());
			function3.jcoErrorCheck();
			JCoTable outTable = function3.getTable("COMMUNICATION");
			iTemp = outTable.getNumRows();
			Date currDate = new Date();
			/*
			 * we check, incase the personnel number is already linked, the
			 * period in which the user is linked.
			 */
			if (iTemp > 0) {
				for (int i = 0; i < iTemp; i++) {
					outTable.setRow(i);
					Date validTo = outTable.getDate("VALIDEND");
					if (validTo.after(currDate)) {
						log.info(_configuration.getMessage("SAP_INFO_EMP_LINKED", empID, outTable.getString("ID"), validTo));
						isCheck = false;
						break;
					}
				}
			}
			if (isCheck) {
				log.info(_configuration.getMessage("USR_NOT_LINKED"));
			}
		} catch (JCoException exception) {
			throw new ConnectorException(exception);
		} catch (ConnectorException exception) {
			throw new ConnectorException(exception);
		} catch (Exception exception) {
			throw new ConnectorException(exception);
		}
		log.info("RETURN");
		return isCheck;
	}
	
	/**
	 * It process child table data like Role, Profile, Parameter and Group.
	 * Includes logic to handle child data from both OIM and OW
	 * Added for Bug 19594110
	 * @param uid
	 * @param childAttrSet
	 * @param isAddAttrVal
	 * @return
	 */
	public Uid processChildTableData(Uid uid, java.util.Set<Attribute> childAttrSet, 
			boolean isAddAttrVal){
		childAttrSet = AttributeUtil.addUid(childAttrSet, uid);
		Name name = AttributeUtil.getNameFromAttributes(childAttrSet);
		uid = AttributeUtil.getUidAttribute(childAttrSet);
		String accountId = null;
		if (uid == null) {
			accountId = name.getNameValue();
		} else {
			accountId = uid.getUidValue();
		}
		SAPCreateUpdateBase createUpdateBase = new SAPCreateUpdateBase(_connection, _configuration,
				_accountAttributes, _filteredAccounts, _tableFormats);
		List<Object> activityGroups = null;
		for (Attribute attr : childAttrSet) {
			String key = attr.getName();
			log.info("valuesToAdd: " +childAttrSet);
			if (SAPConstants.ATTR_ROLES_EMBEDED.equals(key) || 
					SAPConstants.ATTR_PROFILES_EMBEDED.equals(key) || 
					SAPConstants.ATTR_PARAMETERS_EMBEDED.equals(key) || 
					SAPConstants.ATTR_GROUPS_EMBEDED.equals(key)) {
				log.info("Attr: " +attr);
				//log.error("Perf: Adding Embedded Attribute {0} for user {1} " , attr, accountId);
				activityGroups = attr.getValue();
				if(isAddAttrVal){
					createUpdateBase.addAttributeValues(activityGroups, key, accountId);
				} else {
					createUpdateBase.addMultiValueData(activityGroups, key, accountId);
				}
				//log.error("Perf: Added Embedded Attribute {0} for user {1} " , attr, accountId);
			}
		}
		return new Uid(accountId);
	}
	
	
}
