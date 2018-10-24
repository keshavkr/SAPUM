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

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.identityconnectors.common.logging.Log;
import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.framework.common.exceptions.ConnectionFailedException;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeInfo;
import org.identityconnectors.framework.common.objects.AttributeUtil;
import org.identityconnectors.framework.common.objects.EmbeddedObject;
import org.identityconnectors.framework.common.objects.ObjectClass;

import com.sap.conn.jco.JCoException;
import com.sap.conn.jco.JCoRuntimeException;
import com.sap.conn.jco.JCoStructure;
import com.sap.conn.jco.JCoTable;
//Start:: Bug 19567995
import com.sap.conn.jco.JCoParameterList;
//End:: Bug 19567995

/**
 * Base operation class for SAP create and update
 * 
 * @author bfarrell
 * @version 1.0
 * @since 1.0
 */
public class SAPCreateUpdateBase extends AbstractSAPOperation {
	private static final Log log = Log.getLog(SAPCreateUpdateBase.class);
	public SAPConnection _connection;
	public SAPConfiguration _configuration;

	public SAPCreateUpdateBase(SAPConnection conn, SAPConfiguration config,
			Map<String, AttributeInfo> accountAttributes,
			Set<String> filteredAccounts, Map<String, String> tableFormats) {
		super(conn, config, accountAttributes, filteredAccounts, tableFormats);
		_connection = conn;
		_configuration = config;
	}

	/*public SAPCreateUpdateBase() {
		super();

	}*/

	/**
	 * Check if user exists on the SAP system
	 * 
	 * @param identity- name of user
	 * @return - boolean whether the user exists or not
	 */
	public boolean checkIfUserExists(String accountId) {
		log.info("BEGIN");
		boolean exists = false;
		try {
			// Check for the existence of a user
			Function function = new Function("BAPI_USER_EXISTENCE_CHECK",
					accountId, _connection, _configuration
							.getConnectorMessages(), _configuration
							.getEnableCUA(), _configuration.getRetryWaitTime());
			log.info(_configuration.getMessage("SAP_INFO_USR_EXISTENCE_CHK", accountId));
			
			function.execute();
			// To check for a user that does not exist, check the "RETURN" table
			// for code != 124 && code == 88
			JCoStructure returnStruct = function.getStructure("RETURN");
			int number = returnStruct.getInt("NUMBER");
			exists = number != 124 && number == 88;
		} catch (JCoException ex) {
			String emessage = _configuration.getMessage("SAP_ERR_JCO_FUNC_CREATE");
			log.error(emessage, ex);
			ConnectorException ce = new ConnectorException(emessage, ex);
			throw ce;
		}
		log.info("RETURN");
		return exists;
	}

	/**
	 * This method gets the correct create function depending on the version of
	 * SAP.
	 * 
	 * @param accountId
	 * @return
	 */
	protected Function getCorrectCreateFunction(String accountId) {
		log.info("BEGIN");
		// Create the function to create the user. we prefer the create1
		// call but it is "unsupported" in later versions
		Function function = null;
		try {
			function = new Function("BAPI_USER_CREATE1", accountId,
					_connection, _configuration.getConnectorMessages(),
					_configuration.getEnableCUA(), _configuration
							.getRetryWaitTime());
		} catch (JCoException e1) {
			log.ok(_configuration.getMessage("SAP_ERR_BAPI_CREATE_NOT_FOUND"));
			try {
				function = new Function("BAPI_USER_CREATE", accountId,
						_connection, _configuration.getConnectorMessages(),
						_configuration.getEnableCUA(), _configuration
								.getRetryWaitTime());
			} catch (JCoException e2) {
				String msg = _configuration.getMessage("SAP_ERR_CREATE");
				log.error(e2, msg);
				ConnectorException ce = new ConnectorException(msg, e2);
				throw ce;
			} // catch e2
			if (function == null) {
				String msg = _configuration.getMessage("SAP_ERR_CONNECT");
				log.error(e1, msg);
				ConnectionFailedException cfe = new ConnectionFailedException(
						msg, e1);
				throw cfe;
			} // if function == null
		} // catch e1
		log.info("RETURN");
		return function;
	}

	/**
	 * Disable user on resource.
	 * 
	 * @param accountId
	 *            - account to disable
	 * @throws ConnectorException
	 * 
	 *         .
	 */
	public void disableUser(String accountId, Attribute attr){
		log.info("BEGIN");
		try {
			Object value = null;
			if (attr.getValue().get(0).toString().equalsIgnoreCase("true")) {
				try {
					Date dtValidThro = null;
					DateUtil dtUtil = new DateUtil();
					dtValidThro = dtUtil.returnDate("9999-12-31", "yyyy-MM-dd");
					String sDate = dtUtil.parseTime(dtValidThro, "yyyy-MM-dd");
					dtValidThro = dtUtil.returnDate(sDate, "yyyy-MM-dd");
					Timestamp tStart = new Timestamp(dtValidThro.getTime());
					String formattedDateTime = tStart.toString();
					value = formattedDateTime;
		// Start:: Bug 19567995 - WHEN AN USER FROM CUA IS DISABLED AND ENABLED AGAIN, THE USER IS STILL DISABLED
					String lockStatus = getUserLockStatus(accountId);
					
					if(lockStatus.equalsIgnoreCase(_configuration.getdisableLockStatus()));
					{
		        		modifyLockUnlockUser(accountId, false);
		        	}
		// End:: Bug 19567995 - WHEN AN USER FROM CUA IS DISABLED AND ENABLED AGAIN, THE USER IS STILL DISABLED
				} catch (ConnectorException eException) {
					throw ConnectorException.wrap(eException);
				}
			} else {
				Date dtValidThro = new Date();
				DateUtil dtUtil = new DateUtil();
				long mymillidate = dtValidThro.getTime();
				mymillidate = mymillidate - 24 * 60 * 60 * 1000;
				dtValidThro = new Date(mymillidate);
				String sDate = dtUtil.parseTime(dtValidThro, "yyyy-MM-dd");
				dtValidThro = dtUtil.returnDate(sDate, "yyyy-MM-dd");
				value = dtValidThro;

			}
			Function function = new Function("BAPI_USER_CHANGE", accountId,
					_connection, _configuration.getConnectorMessages(),
					_configuration.getEnableCUA(), _configuration
							.getRetryWaitTime());
			function.setImportValue("LOGONDATA", "GLTGB", value, true);
			function.executeWithRetry(_configuration.getMaxBAPIRetries());
			function.jcoErrorCheck();
		} catch (JCoException ex) {
			throw new ConnectorException(ex);
		}
		catch (ConnectorException ex) {
			throw new ConnectorException(ex);
		}
		log.info("RETURN");
	}

	/**
	 *This method will be called both OIM and OW. 
	 * @param accountId
	 * @param subSystems
	 * @throws JCoException
	 * @throws ConnectorException
	 */
	protected void maintainSapSubSystems(String accountId,
			List<Object> subSystems) throws JCoException, ConnectorException {
		log.info("BEGIN");
		if (accountId != null && subSystems != null
				&& _configuration.getEnableCUA()) {
			Function function = null;
			try {
				function = new Function("BAPI_USER_SYSTEM_ASSIGN", accountId,
						_connection, _configuration.getConnectorMessages(),
						_configuration.getEnableCUA(), _configuration
								.getRetryWaitTime());
				function.setImportValue("SYSTEMS", "SUBSYSTEM", subSystems,
						false);
			} catch (JCoException e) {
				String message = null;
				int group = e.getGroup();
				if (group != JCoException.JCO_ERROR_FUNCTION_NOT_FOUND) {
					message = _configuration.getMessage("SAP_ERR_CUASYSTEMS",
							accountId);
				} else {
					message = _configuration
							.getMessage("SAP_ERR_NO_SUBSYS_BAPI");
				} // group != JCO_ERROR_FUNCTION_NOT_FOUND
				log.error(e, message);
				ConnectorException ce = new ConnectorException(message, e);
				throw ce;
			}
			function.executeWithRetry(_configuration.getMaxBAPIRetries());
			function.jcoErrorCheck();
		}
		log.info("RETURN");
	}

	/**
	 * This method handles normal SAP profile assignment as well as hands off
	 * subsystem profile assignment.
	 * 
	 * @param accountId
	 * @param profiles
	 * @throws ConnectorException
	 */
	protected void maintainSapProfiles(String accountId, List<Object> profiles)
			throws ConnectorException {
		log.info("BEGIN");
		if (accountId != null) {
			if (!_configuration.getEnableCUA()) {
				if (profiles != null) {
					try {
						String tableName = "PROFILES";
						String format = _tableFormats.get(tableName);
						if (format == null) {
							String message = _configuration.getMessage(
									"SAP_ERR_TABLE_FORMAT", tableName);
							log.error(message);
							throw new ConnectorException(message);
						}
						Function function = new Function(
								"BAPI_USER_PROFILES_ASSIGN", accountId,
								_connection, _configuration
										.getConnectorMessages(), _configuration
										.getEnableCUA(), _configuration
										.getRetryWaitTime());
						JCoTable profilesTable = function.getTable(tableName);
						List<String> profRows = SAPUtil.getComplexFormat(
								format, SAPConnector.ESCAPED_COMPLEX_DELIMITER,
								false);
						if (!profRows.get(0).equals("BAPIPROF")) {
							String message = _configuration.getMessage(
									"SAP_ERR_PROFILE_FORMAT", "Base Format");
							log.error(message);
							throw new ConnectorException(message);
						}
						for (Object profile : profiles) {
							if (profile != null) {
								Iterator<String> profAttrs = Arrays
										.asList(
												profile
														.toString()
														.split(
																SAPConnector.ESCAPED_COMPLEX_DELIMITER))
										.iterator();
								for (String row : profRows) {
									String attr = profAttrs.next();
									if (row.equals("BAPIPROF")) {
										function.appendTableRow(profilesTable,
												row, attr);
									} else {
										if (attr != null && !attr.equals("")) {
											function.setTableRowValue(
													profilesTable, row, attr);
										}
									}
									
									log.info(_configuration.getMessage("SAP_INFO_SET_ROW_ATTR", row, attr));
								}
							}
						}
						function.executeWithRetry(_configuration
								.getMaxBAPIRetries());
						function.jcoErrorCheck();
					} catch (JCoException e) {
						String message = _configuration.getMessage(
								"SAP_ERR_PROFILE_MGMT", accountId);
						log.error(message);
						ConnectorException ce = new ConnectorException(message,
								e);
						throw ce;
					}
				}
			} else {
				maintainSubSystemProfiles(accountId, profiles);
			}
		}
		log.info("RETURN");
	}

	/**
	 * Set the CUA subsystem profiles using BAPI_USER_LOCPROFILES_ASSIGN. This
	 * will set the distribution systems and profiles - any already set will be
	 * eliminated.
	 * 
	 * @param accountId
	 * @param profiles
	 * @throws ConnectorException
	 */
	protected void maintainSubSystemProfiles(String accountId,
			List<Object> profiles) throws ConnectorException {
		log.info("BEGIN");
		if (accountId != null && profiles != null) {
			try {
				String tableName = "PROFILES";
				String format = _tableFormats.get(tableName);
				if (format == null) {
					String message = _configuration.getMessage(
							"SAP_ERR_TABLE_FORMAT", tableName);
					log.error(message);
					throw new ConnectorException(message);
				}
				Function function = new Function(
						"BAPI_USER_LOCPROFILES_ASSIGN", accountId, _connection,
						_configuration.getConnectorMessages(), _configuration
								.getEnableCUA(), _configuration
								.getRetryWaitTime());
				JCoTable profilesTable = function.getTable(tableName);
				List<String> profRows = SAPUtil.getComplexFormat(format,
						SAPConnector.ESCAPED_COMPLEX_DELIMITER, false);
				if (!profRows.get(0).equals("PROFILE")) {
					String message = _configuration.getMessage(
							"SAP_ERR_PROFILE_FORMAT", "Base Format");
					log.error(message);
					throw new ConnectorException(message);
				}
				for (Object profile : profiles) {
					if (profile != null) {
						Iterator<String> profAttrs = Arrays
								.asList(
										profile
												.toString()
												.split(
														SAPConnector.ESCAPED_COMPLEX_DELIMITER))
								.iterator();
						String subSystem = profAttrs.next();
						function.appendTableRow(profilesTable, "SUBSYSTEM",
								subSystem);
						for (String row : profRows) {
							String attr = profAttrs.next();
							if (attr != null && !attr.equals("")) {
								function.setTableRowValue(profilesTable, row,
										attr);
							}
							
							log.info(_configuration.getMessage("SAP_INFO_SET_ROW_ATTR", row, attr));
						}
					}
				}
				function.executeWithRetry(_configuration.getMaxBAPIRetries());
				function.jcoErrorCheck();
			} catch (JCoException e) {
				String message = _configuration.getMessage(
						"SAP_ERR_LOC_PROFILE_MGMT", accountId);
				log.error(message);
				ConnectorException ce = new ConnectorException(message, e);
				throw ce;
			}
		}
		log.info("RETURN");
	}

	/**
	 * This method handles the normal SAP activity group assignement as well as
	 * hands off subsystem activity group assignment.
	 * 
	 * @param accountId
	 * @param activityGroups
	 * @throws ConnectorException
	 */
	protected void maintainSapActivityGroups(String accountId,
			List<Object> activityGroups) throws ConnectorException {
		log.info("BEGIN");
		if (accountId != null) {
			if (!_configuration.getEnableCUA()) {
				if (activityGroups != null) {
					try {
						String tableName = "ACTIVITYGROUPS";
						String format = _tableFormats.get(tableName);
						if (format == null) {
							String message = _configuration.getMessage(
									"SAP_ERR_TABLE_FORMAT", tableName);
							log.error(message);
							throw new ConnectorException(message);
						}
						Function function = new Function(
								"BAPI_USER_ACTGROUPS_ASSIGN", accountId,
								_connection, _configuration
										.getConnectorMessages(), _configuration
										.getEnableCUA(), _configuration
										.getRetryWaitTime());
						JCoTable actgrpTable = function.getTable(tableName);
						List<String> actgrpRows = SAPUtil.getComplexFormat(
								format, SAPConnector.ESCAPED_COMPLEX_DELIMITER,
								false);
						if (!actgrpRows.get(0).equals("AGR_NAME")) {
							String message = _configuration.getMessage(
									"SAP_ERR_ACTGRP_FORMAT", "Base Format");
							log.error(message);
							throw new ConnectorException(message);
						}
						for (Object activityGroup : activityGroups) {
							if (activityGroup != null) {
								Iterator<String> actgrpAttrs = Arrays
										.asList(
												activityGroup
														.toString()
														.split(
																SAPConnector.ESCAPED_COMPLEX_DELIMITER))
										.iterator();
								for (String row : actgrpRows) {
									String attr = actgrpAttrs.next();
									if (row.equals("AGR_NAME")) {
										function.appendTableRow(actgrpTable,
												row, attr);
									} else {
										if (attr != null && !attr.equals("")
												&& !attr.equals(" ")) {
											if (row.equals("TO_DAT")
													|| row.equals("FROM_DAT")) {											
												Date date = SAPUtil
														.stringToDate(attr,
																"MM/dd/yyyy");
												function.setTableRowValue(
														actgrpTable, row, date);
											} else {
												function.setTableRowValue(
														actgrpTable, row, attr);
											}
										}
									}
									
									log.info(_configuration.getMessage("SAP_INFO_SET_ROW_ATTR", row, attr));
								}
							}
						}
						function.executeWithRetry(_configuration
								.getMaxBAPIRetries());
						function.jcoErrorCheck();
					} catch (JCoException e) {
						String message = _configuration.getMessage(
								"SAP_ERR_ACTGRP_MGMT", accountId);
						log.error(message);
						ConnectorException ce = new ConnectorException(message,
								e);
						throw ce;
					}
				}
			} else {
				maintainSubSystemActivityGroups(accountId, activityGroups);
			}
		}
		log.info("RETURN");
	}

	/**
	 * Set the CUA subsystem activity groups using
	 * BAPI_USER_LOCACTGROUPS_ASSIGN. This will set the distribution systems and
	 * activity groups - any already set will be eliminated.
	 * 
	 * @param accountId
	 * @param activityGroups
	 * @throws ConnectorException
	 */
	protected void maintainSubSystemActivityGroups(String accountId,
			List<Object> activityGroups) throws ConnectorException {
		log.info("BEGIN");
		if (accountId != null && activityGroups != null) {
			try {
				String tableName = "ACTIVITYGROUPS";
				String format = _tableFormats.get(tableName);
				if (format == null) {
					String message = _configuration.getMessage(
							"SAP_ERR_TABLE_FORMAT", tableName);
					log.error(message);
					throw new ConnectorException(message);
				}
				Function function = new Function(
						"BAPI_USER_LOCACTGROUPS_ASSIGN", accountId,
						_connection, _configuration.getConnectorMessages(),
						_configuration.getEnableCUA(), _configuration
								.getRetryWaitTime());
				JCoTable actgrpTable = function.getTable(tableName);
				List<String> actgrpRows = SAPUtil.getComplexFormat(format,
						SAPConnector.ESCAPED_COMPLEX_DELIMITER, false);
				if (!actgrpRows.get(0).equals("AGR_NAME")) {
					String message = _configuration.getMessage(
							"SAP_ERR_ACTGRP_FORMAT", "Base Format");
					log.error(message);
					throw new ConnectorException(message);
				}
				for (Object activityGroup : activityGroups) {
					if (activityGroup != null) {
						Iterator<String> actgrpAttrs = Arrays
								.asList(
										activityGroup
												.toString()
												.split(
														SAPConnector.ESCAPED_COMPLEX_DELIMITER))
								.iterator();
						String subSystem = actgrpAttrs.next();
						function.appendTableRow(actgrpTable, "SUBSYSTEM",
								subSystem);
						for (String row : actgrpRows) {
							String attr = actgrpAttrs.next();
							if (attr != null && !attr.equals("")
									&& !attr.equals(" ")) {
								if (row.equals("TO_DAT")
										|| row.equals("FROM_DAT")) {
									Date date = SAPUtil.stringToDate(attr,
											"MM/dd/yyyy");
									function.setTableRowValue(actgrpTable, row,
											date);
								} else {
									function.setTableRowValue(actgrpTable, row,
											attr);
								}
							}
							
							log.info(_configuration.getMessage("SAP_INFO_SET_ROW_ATTR", row, attr));
						}
					}
				}
				function.executeWithRetry(_configuration.getMaxBAPIRetries());
				function.jcoErrorCheck();
			} catch (JCoException e) {
				String message = _configuration.getMessage(
						"SAP_ERR_LOC_ACTGRP_MGMT", accountId);
				log.error(message);
				ConnectorException ce = new ConnectorException(message, e);
				throw ce;
			}
		}
		log.info("RETURN");
	}

	/**
	 * Get the user SAP password.
	 * 
	 * @param attrMap
	 * @param type
	 * @return the unencryped user password
	 */
	protected String getPassword(Map<String, Attribute> attrMap, String type) {
		log.info("BEGIN");
		Attribute passwd = attrMap.get(type);
		String password = null;
		if (passwd != null) {
			GuardedString pwd = AttributeUtil.getGuardedStringValue(attrMap
					.get(type));
			final char[] _array = new char[50];
			pwd.access(new GuardedString.Accessor() {
				public void access(char[] clearChars) {
					try {
				        System.arraycopy(clearChars, 0, _array, 0, clearChars.length); 
					} catch (Exception sException) {
						log.error(sException.getMessage());
					}
            	}
			});
			password = new String(_array).trim();
			// if uppercase password attribute is true
			if (_configuration.getUpperCasePwd()) {
				log.ok(_configuration.getMessage("SAP_INFO_UPPERCASE_PWD"));
				password = password.toUpperCase();
			}
		}
		log.info("RETURN");
		return password;
	}

	/**
	 * SAP password are expired in the default case. This method is called to
	 * explicitly unexpire the password. The other way that this could be
	 * handled is to set the last login time. However, with SAP Note 750390,
	 * LTIME in no longer writeable.
	 * 
	 * @param accountID
	 * @param currentPassword
	 * @param newPassword
	 * @throws ConnectorException
	 * @throws JCoException
	 */
	protected void unexpirePassword(String accountId, String currentPassword,
			String newPassword) throws ConnectorException, JCoException {
		log.info("BEGIN");
		if (currentPassword != null && newPassword != null) {
			realPasswordChange(accountId, new GuardedString(currentPassword.toCharArray()), new GuardedString(newPassword.toCharArray()), true);
		} else {
			newPassword = null;
			currentPassword = null;
			// The current password and the new password are required.
			String message = _configuration
					.getMessage("SAP_ERR_UNEXPIRE_PASSWD");
			log.error(message);
			throw new ConnectorException(message);
		}
		newPassword = null;
		currentPassword = null;
		log.info("RETURN");
	}

	/**
	 * perform the real password change as the user on the system(s) This method
	 * should not be called directly. It implements the real change and is
	 * called from {<@link #unexpirePassword(String, String, String)} and
	 * {@link #changeUserPassword(String, Map)}
	 * 
	 * @param accountId
	 * @param currentPassword
	 *            - current clear text password
	 * @param newPassword
	 *            - new clear text password
	 * @param unExpire
	 *            - boolean to show if this is an unexpire action or not
	 * @throws ConnectorException
	 * @throws JCoException
	 */
	protected void realPasswordChange(String accountId, GuardedString currentPassword,
			GuardedString newPassword, boolean unExpire) throws ConnectorException,
			JCoException {
		log.info("BEGIN");
		// This method will perform the same way as if a user tries to
		// change their password themselves in SAP.
		// If the new password meets any of the conditions below,
		// the operations will fail.
		// 
		// 1. contains illegal characters
		// 2. first character is a '?'
		// 3. is too short
		// 4. first 3 characters are equal
		// 5. 'PASS' and 'SAP*' not allowed
		// 6. less # digits than required (SAP configuration parameter)
		// 7. less # letters than required (SAP configuration parameter)
		// 8. less # specials than required (SAP configuration parameter)
		// 9. too similar to old password
		// 10. is in table USR40 (forbidden pattern)
		// 11. is in user's password history (currently the last 5 passwords)
		//
		// change productive password on a child system
		// special case the CUA child password change. Productive passwords
		// are not replicated to child systems. If we have CUA enabled and
		// the RFC is set in the config then try to change each assigned
		// child.
		boolean useBAPIReturn = false;
		// check if we support SAP note 899614 on the underlying FM
		/*Function function = new Function("SUSR_USER_CHANGE_PASSWORD_RFC",
				_connection, _configuration.getConnectorMessages(),
				_configuration.getEnableCUA(), _configuration
						.getRetryWaitTime());*/
		Function function = new Function("SUSR_USER_CHANGE_PASSWORD_RFC",
				_connection, _configuration.getConnectorMessages(),
				true, _configuration
						.getRetryWaitTime());
		// if the field does not exist the getImportParameterList will throw
		try {
			useBAPIReturn = function.getImportParameterList().getMetaData()
					.hasField("USE_BAPI_RETURN");
			if (useBAPIReturn) {
				log.info(_configuration.getMessage("SAP_INFO_USE_BAPI_RETURN"));
				
			} else {
				log.info(_configuration.getMessage("SAP_INFO_USE_EXCEPTION_RETURN"));
				
			}
		} catch (JCoRuntimeException e) {
				log.error(_configuration.getMessage("SAP_INFO_USE_EXCEPTION_RETURN", e));
			// nothing to do just trace to make it clear
		}

		String rfc = _configuration.getCuaChildPasswordChangeFuncModule();
		if (_configuration.getEnableCUA() && rfc != null && rfc.length() != 0) {
		//if (true && _configuration.getEnableCUA())
		//{
		childPasswordChange(accountId, currentPassword, newPassword,
					unExpire, useBAPIReturn);
		} else if (unExpire) {
			// only do it if we are unexpiring. the expired is handled in the
			// normal update

			// use the standard FM to change the password
			function.setImportValue(null, "BNAME", accountId, false);
			function.setImportValue(null, "PASSWORD", currentPassword, false);
			function.setImportValue(null, "NEW_PASSWORD", newPassword, false);
			// check if support for SAP note 899614 is installed
			try {
				if (function.getExportParameterList().getMetaData().hasField(
						"USE_BAPI_RETURN")) {
					function.setImportValue(null, "USE_BAPI_RETURN", true,
							false);
					function.setImportValue(null, "USE_NEW_EXCEPTION", true,
							false);
				}
			} catch (JCoRuntimeException e) {
				// Don't want to fail because the field can't be set.
				// nothing to do just log to make it clear
				log.info("Using exceptions for return values");
			} catch (JCoException e) {
				log.info("Using exceptions for return values");
			} // try catch JCoException

			try {
				function.executeWithRetry(_configuration.getMaxBAPIRetries());
				// handle the case that we DO NOT have the SAP note 899614
				// this keeps backwards compatibility alive
			} catch (JCoException e) {
				log.error(e, "SAP_ERR_CHANGING_PASSWD");
				if (_configuration.getReturnSAPTemporaryPwdsOnFailure()) {
					String message = _configuration.getMessage(
							"SAP_ERR_DISPLAY_TMP_PWD", currentPassword);
					log.error(message);
					throw new ConnectorException(message, e);
				}
				throw new ConnectorException(e);
			}
			// handle the case that we DO have the SAP note 899614
			// exception are handled by the FM and we just get the errors
			// back in the RETURN table
			// NOTE: this makes the failure in the create far clearer, we
			// still fail the create but now with the message that the
			// unexpire failed and not a general creation error
			try {
				function.jcoErrorCheck();
			} catch (ConnectorException ce) {
				// simulate the JCO exception behaviour
				String message;
				if (unExpire) {
					StringBuffer sb = new StringBuffer();
					if (_configuration.getReturnSAPTemporaryPwdsOnFailure()) {
						sb.append(_configuration.getMessage(
								"SAP_ERR_DISPLAY_TMP_PWD", currentPassword));
						sb.append("/n");
					}
					sb.append(_configuration
							.getMessage("SAP_ERR_UNEXPIRING_PASSWD"));
					message = sb.toString();
				} else {
					message = _configuration
							.getMessage("SAP_ERR_CHANGING_PASSWD");
				}
				log.error(ce, message);
				throw new ConnectorException(message, ce);
			}
		}
		log.info("RETURN");
	}

	/**
	 * Change the password on a child cua system.
	 * 
	 * @param accountId
	 * @param currentPassword
	 * @param newPassword
	 * @param unExpire
	 * @param useBAPIReturn
	 * @throws JCoException
	 * @throws ConnectorException
	 */
	private void childPasswordChange(String accountId, GuardedString currentPassword,
			GuardedString newPassword, boolean unExpire, boolean useBAPIReturn)
			throws JCoException, ConnectorException {
		log.info("BEGIN");
		Function function = null;
		String rfcName = _configuration.getCuaChildPasswordChangeFuncModule();
		if (_configuration.getEnableCUA() && rfcName != null
				&& rfcName.length() != 0) {
			log.info(_configuration.getMessage("SAP_INFO_CUA_PSWD_CHANGE"));
			if (unExpire) {
				// Do the best we can to ensure that the password has been
				// propagated
				// the each child.

				// TODO: this is ugly but for now we need to wait here for a
				// while to allow for iDOC processing. This should be converted
				// into parallel processing of the children with retries and
				// not with this sleep
				int checkDelay = _configuration.getCuaChildPasswordCheckDelay();
				try {
					Thread.sleep(checkDelay);
				} catch (InterruptedException ie) {
					// ignore it for now
				}
			}
			// get the list of children (no empty strings in the list)
			ArrayList<String> children = SAPUtil.getSubSystems(accountId, null,
					_connection, _configuration);
			if (children != null && children.size() != 0) {
				// keep track of a change failure try all systems before we
				// really
				// fail unless we have a FM issue, not a password change issue
				boolean failedOnce = false;
				ConnectorException ce = null;
				for (String child : children) {
					if (child == null) {
						continue;
					} // if child
					if (unExpire) {
						// Do the best we can to ensure that the password has
						// been propagated
						// the each child. Call a FM on the CUA master that will
						// see if the iDoc
						// has been delivered and check the state and value of
						// the user's password.
						// state should be expired (Initial), value should be a
						// temporary value
						// generated in realCreate/realUpdate.
						// Note: A non-admin change password should never set
						// unExpire to true
						// so this should never be called from a end-user change
						// password operation
						String rfcNameCheck = _configuration
								.getCuaChildPasswordCheckFuncModule();
						if (rfcNameCheck != null && rfcNameCheck.length() != 0) {
							
							log.info(_configuration.getMessage("SAP_INFO_CHK_PASSWD_CHG_CHILD"));
							
							

							try {
								function = new Function(rfcNameCheck,
										_connection, _configuration
												.getConnectorMessages(),
										_configuration.getEnableCUA(),
										_configuration.getRetryWaitTime());
								function.setImportValue(null, "BNAME",
										accountId, false);
								function.setImportValue(null, "CHILD_SYSTEM",
										child, false);
								function.executeWithRetry(_configuration
										.getMaxBAPIRetries());
								int pwdState = function
										.getExportParameterList().getInt(
												"PWDSTATE");
								if (pwdState != 0) {
									failedOnce = true;
									String message = _configuration
											.getMessage(
													"SAP_ERR_PASSWD_CHECK_CHILD",
													child);
									ce = SAPUtil.addConnectorExceptionMessage(
											ce, message);
								} // if pwdState
							} catch (JCoException e) {
								String message;
								int group = e.getGroup();
								if (group != JCoException.JCO_ERROR_FUNCTION_NOT_FOUND) {
									failedOnce = true;
									message = _configuration
											.getMessage(
													"SAP_ERR_PASSWD_CHECK_CHILD",
													child);
									ce = SAPUtil.addConnectorExceptionMessage(
											ce, message);
									log.warn(_configuration.getMessage("SAP_WARN_INIT_PASSWD_NOT_SET", child));
									
									
								} else {
									message = _configuration.getMessage(
											"SAP_ERR_JCO_PASSWD_FUNCTION",
											rfcNameCheck);
									log.error(message);
									ConnectorException ce2 = new ConnectorException(
											message, e);
									throw ce2;
								} // if..else group
							} // try catch JCoException
						} else {
							log.info(_configuration.getMessage("SAP_INFO_NO_MOD_FOR_PWD_CHG_CUA"));
							
						} // if..else rfcNameCheck
					}
					try {
						// create the function
						function = new Function(rfcName, _connection,
								_configuration.getConnectorMessages(),
								_configuration.getEnableCUA(), _configuration
										.getRetryWaitTime());
						function
								.setImportValue(null, "BNAME", accountId, false);
						function.setImportValue(null, "PASSWORD",
								currentPassword, false);
						function.setImportValue(null, "NEW_PASSWORD",
								newPassword, false);
						if (useBAPIReturn) {
							function.setImportValue(null, "USE_BAPI_RETURN",
									true, false);
							function.setImportValue(null, "USE_NEW_EXCEPTION",
									true, false);
						} // if useBAPIReturn
						// only add it if the child is a real string, the
						// central
						// system is not in the list and is added as an empty
						// string
						if (child != "") {
							function.setImportValue(null, "CHILD_SYSTEM",
									child, false);
						} // if child
						// we expect that the SAP note 899614 is installed, just
						// be cautious and catch JCO exceptions, they point to
						// invocation problems (i.e. child not found)
						function.executeWithRetry(_configuration
								.getMaxBAPIRetries());
					} catch (JCoException e) {
						String message;
						int group = e.getGroup();
						if (group != JCoException.JCO_ERROR_FUNCTION_NOT_FOUND) {
							message = _configuration.getMessage(
									"SAP_ERR_JCO_PASSWORD_CHILD", rfcName);
						} else {
							message = _configuration.getMessage(
									"SAP_ERR_JCO_PASSWD_FUNCTION", rfcName);
						}
						ce = new ConnectorException(message, e);
						log.error(message, e);
						throw ce;
					} // try catch JCoException
					if (useBAPIReturn) {
						try {
							function.jcoErrorCheck();
						} catch (ConnectorException ce2) {
							String message = null;
							if (unExpire) {
								if (_configuration
										.getReturnSAPTemporaryPwdsOnFailure()) {
									ce = SAPUtil.addConnectorExceptionMessage(
											ce2, _configuration.getMessage(
													"SAP_ERR_DISPLAY_TMP_PWD",
													currentPassword));
								}
								message = _configuration.getMessage(
										"SAP_ERR_PASSWD_UNEXPIRE_CHILD", child);
							} else {
								message = _configuration.getMessage(
										"SAP_ERR_PASSWD_CHANGE_CHILD", child);
							}
							failedOnce = true;
							ce = SAPUtil.addConnectorExceptionMessage(ce, ce2
									.getMessage());
							ce = SAPUtil.addConnectorExceptionMessage(ce,
									message);
							log.info(_configuration.getMessage("SAP_INFO_CHILD_PWDCHG_FAILED",child));
							
							
						}
					}
				} // for each
				// if we have failed at least one throw
				if (failedOnce) {
					log.error(ce.getMessage());
					throw ce;
				} // if failedOnce
			}
		}
		log.info("RETURN");
	}

	/**
	 * This method generates a temporary password.
	 * 
	 * @param user
	 * @return a generated temporary password
	 */
	protected String generateTempPassword() {
		log.info("BEGIN");
		String pwd = null;
		if (_configuration.getUseSAPTempPwd()) {
			log.info("Calling SUSR_GENERATE_PASSWORD to generate a password");
			// Ask SAP to generate a password.
			// SUSR_GENERATE_PASSWORD requires SAP Note 832661 (SAP 6.20 Support
			// Package 51)
			// *** This SAP Note is only available to for SAP 6.20, 6.40 as of
			// (04/27/2005) ***
			// *** It will be available for SAP 7.0 at some point. It is unknown
			// at this ***
			// *** point if this Note will be available for SAP versions less
			// than 6.20 ***
			// (Got this tip from SAP development support.)
			Function function = null;
			try {
				function = new Function("SUSR_GENERATE_PASSWORD", _connection,
						_configuration.getConnectorMessages(), _configuration
								.getEnableCUA(), _configuration
								.getRetryWaitTime());
				function.executeWithRetry(_configuration.getMaxBAPIRetries());
				pwd = function.getExportParameterList().getString("PASSWORD");
				log.info(_configuration.getMessage("SAP_INFO_PWD_CRTD"));
			} catch (JCoException e) {
				String message = null;
				
				log.error(e,_configuration.getMessage("SAP_ERR_WHILE_GEN_TEMP_PWD"));
				int group = e.getGroup();
				if (group == JCoException.JCO_ERROR_COMMUNICATION) {
					message = _configuration.getMessage(
							"SAP_ERR_JCO_FUNC_NOT_ISNTL",
							"SUSR_GENERATE_PASSWORD");
					log.error(message);
					// this occurs b/c SUSR_GENERATE_PASSWORD is not
					// installed on the SAP system (requires SAP Note 832661)
				} else if (group == JCoException.JCO_ERROR_SYSTEM_FAILURE) {
					message = _configuration.getMessage(
							"SAP_ERR_JCO_FUNC_NOT_RENBL",
							"SUSR_GENERATE_PASSWORD");
					log.error(message);
					// this occurs b/c SUSR_GENERATE_PASSWORD is not
					// remote enabled. just eat it in that case.
				}
				function = null;
			}
		}

		// if we dont have a generated password
		// use the resource attribute password
		// we really only want this for testing
		if (pwd == null) {
			final char[] _array = new char[50];
			_configuration.getTempPassword().access(new GuardedString.Accessor() {
            	public void access(char[] clearChars) {
					try {
				        System.arraycopy(clearChars, 0, _array, 0, clearChars.length); 
						//destProps.put(JCO_PASSWD, new String(clearChars));	
					} catch (Exception sException) {
						log.error(sException.getMessage());
					}
            	}
			});	
			pwd = new String(_array).trim();
			/*GuardedStringAccessor accessor = new GuardedStringAccessor();
			GuardedString tempPwd = _configuration.getTempPassword();
			tempPwd.access(accessor);
			pwd = new String(accessor.getArray());
			accessor.clear();*/
		}
		log.info("RETURN");
		return pwd;
	}

	/**
	 * Ensure that the password meets basic SAP system password policy. NOTE: If
	 * PASSWORD_FORMAL_CHECK is not remote-enabled or not available on the SAP
	 * system, this method simply returns that the password was verified. There
	 * is no way to verify it, so just assume it is good because this is just an
	 * extra check
	 * 
	 * @param password
	 *            - the password to check
	 * @throws JCoException
	 *             if the password does not meet password policy
	 */
	protected void validatePassword(GuardedString password) throws JCoException {
		log.info("BEGIN");
		// To determine if a user's password passes SAP system password
		// policy. PASSWORD_FORMAL_CHECK must be remote enabled
		try {
			Function function = new Function("PASSWORD_FORMAL_CHECK",
					_connection, _configuration.getConnectorMessages(),
					_configuration.getEnableCUA(), _configuration
							.getRetryWaitTime());
			function.setImportValue("PASSWORD", "BAPIPWD", password, false);
			function.executeWithRetry(_configuration.getMaxBAPIRetries());
		} catch (JCoException e) {
			int group = e.getGroup();
			// if group != JCO_ERROR_SYSTEM_FAILURE occurs,
			// then PASSWORD_FORMAL_CHECK is not remote enabled.
			// Just eat the exception in that case.
			if (group != JCoException.JCO_ERROR_SYSTEM_FAILURE) {
				// In some older SAP systems (like 4.6x) PASSWORD_FORMAL_CHECK
				// can't be installed. When the fuction is created the null
				// template check will throw a JCO_ERROR_FUNCTION_NOT_FOUND
				// exception this one should also be eaten and not rethrown
				String message = _configuration.getMessage(
						"SAP_ERR_JCO_FUNC_NOT_FOUND", "PASSWORD_FORMAL_CHECK");
				if (group != JCoException.JCO_ERROR_FUNCTION_NOT_FOUND) {
					log.error(e, message);
					throw e;
				} else {
					log.info("SAP_ERR_JCO_FUNC_NOT_ISNTL",
							"PASSWORD_FORMAL_CHECK");
				}
			}
			log.info("SAP_ERR_JCO_FUNC_NOT_RENBL", "PASSWORD_FORMAL_CHECK");
		}
		log.info("RETURN");
	}



	/**
	 * Description: Gets the role and profile information for the SAP target
	 * system
	 * 
	 * @param sUserID
	 *            Account ID for which Role and Profile information is required.
	 *            For example: John.Doe
	 * @param sStructure
	 *            Structure name of role or profile. For example:ACTIVITYGROUPS
	 * @param _connection
	 *            Connection reference
	 * @param _configuration
	 *            
	 * @return JCoTable Returns JCOTable having the Role or Profile information
	 */
	public JCoTable getRoleorProfile(String sUserID, String sStructure, SAPConnection _connection, SAPConfiguration _configuration) {
		log.info("BEGIN");
		JCoTable multiValuesTable = null;
		try {			
				String sBAPIName;
					
				if(_configuration.getEnableCUA()){
					if (sStructure.equalsIgnoreCase("ACTIVITYGROUPS")) {
						sBAPIName = "BAPI_USER_LOCACTGROUPS_READ";
					} else if (sStructure.equalsIgnoreCase("PROFILES")) {
						sBAPIName = "BAPI_USER_LOCPROFILES_READ";
					} else {
						sBAPIName = "BAPI_USER_GET_DETAIL";
					}
				} else {
					sBAPIName = "BAPI_USER_GET_DETAIL";
				}			
				Function function = new Function(sBAPIName, true, 
						_connection, _configuration
						.getConnectorMessages(), false, _configuration.getRetryWaitTime());
				function.setImportField("USERNAME",	sUserID);                
				function.execute();				
				multiValuesTable = function.getTableParameterList()
						.getTable(sStructure);
			
		} catch (Exception e) {
			log.error("Error occured while getting roles/profiles from target {0}",e.getMessage());
			throw new ConnectorException(e.getMessage());
		}
		log.info("RETURN");
		return multiValuesTable;
	}

	/**
	 * Description:This method is used to get child table data entered
	 * 
	 * @param sUserID
	 *            User ID of the user
	 * @param sStructureName
	 *            SAP Structure name
	 * 
	 * @return String
	 * @throws ConnectorException
	 */
	private JCoTable getChildData(String sUserID, String sStructureName,
			String sBAPIName) throws ConnectorException {
		log.info("BEGIN");
		JCoTable jcoTable = null;
		String sMethodName = "getChildData()";
		try {
			Function function = new Function(sBAPIName, sUserID, _connection,
					_configuration.getConnectorMessages(), _configuration
							.getEnableCUA(), _configuration.getRetryWaitTime());
			function.executeWithRetry(_configuration.getMaxBAPIRetries());
			jcoTable = function.getTable(sStructureName);
		} catch (JCoException exception) {
			throw new ConnectorException(exception);
		} catch (ConnectorException exception) {
			throw new ConnectorException(exception);
		} catch (Exception exception) {
			throw new ConnectorException(exception);
		}
		log.info("RETURN");
		return jcoTable;
	}

	public void modifyLockUnlockUser(String sUserId, boolean sAttributeValue)
			throws ConnectorException {
		log.info("BEGIN");
		Function function;
		try {
			if (sAttributeValue) {
				function = new Function("BAPI_USER_LOCK", sUserId, _connection,
						_configuration.getConnectorMessages(), _configuration
								.getEnableCUA(), _configuration
								.getRetryWaitTime());
			} else {
				function = new Function("BAPI_USER_UNLOCK", sUserId,
						_connection, _configuration.getConnectorMessages(),
						_configuration.getEnableCUA(), _configuration
								.getRetryWaitTime());
			}
			function.executeWithRetry(_configuration.getMaxBAPIRetries());
			function.jcoErrorCheck();
		} catch (JCoException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		log.info("RETURN");
	}

	/**
	 * This method handles the normal SAP activity group assignement as well as
	 * hands off subsystem activity group assignment.
	 * 
	 * @param accountId
	 * @param activityGroups
	 * @throws ConnectorException
	 */
	protected void addMultiValueData(List<Object> activityGroups ,
			String key, String accountId) throws ConnectorException {
		log.info("BEGIN");
		SimpleDateFormat dateFormatter = new SimpleDateFormat("MM/dd/yyyy");
		int ch = '~';	
			try {
				String bapiName = SAPUtil.getAddMultiValueDataBAPIName(key, _configuration.getEnableCUA());
				String tableName = null;
				if(key.equalsIgnoreCase("roles")){
					tableName = _configuration.getRoles().substring(0,_configuration.getRoles().lastIndexOf(ch));
					//tableName = "ACTIVITYGROUPS";
				} else if(key.equalsIgnoreCase("profiles")){
					tableName = _configuration.getProfiles().substring(0,_configuration.getProfiles().lastIndexOf(ch));					
					//tableName = "PROFILES";
				} else if(key.equals("parameters")){
					tableName = _configuration.getParameters().substring(0,_configuration.getParameters().lastIndexOf(ch));					
					//tableName = "PARAMETER1";
				} else if(key.equals("groups")){
					tableName = _configuration.getGroups().substring(0,_configuration.getGroups().lastIndexOf(ch));					
					//tableName = "GROUPS";
				}
				
				Function function = new Function(
						bapiName, accountId.toUpperCase(),
						_connection, _configuration.getConnectorMessages(),
						_configuration.getEnableCUA(), _configuration
								.getRetryWaitTime());
				JCoTable actgrpTable = function.getTable(tableName);
				//int j = 0;
				int j = 0;
				if(activityGroups != null){
					for(int i=0;i<activityGroups.size();i++){
						j = 0;
						EmbeddedObject activityGrpBuilder=(EmbeddedObject) activityGroups.get(i); 
						ObjectClass embeddedObjectClass=activityGrpBuilder.getObjectClass();
						Set<Attribute> attrAct= activityGrpBuilder.getAttributes(); 
						Iterator<Attribute> iter = attrAct.iterator();
						while (iter.hasNext()) {
							Attribute atr=(Attribute) iter.next(); 
							String targetAttrName=atr.getName();
							if(targetAttrName.equalsIgnoreCase("PROFILE") && !_configuration.getEnableCUA()){
								targetAttrName = "BAPIPROF";
							}
							if(targetAttrName.equalsIgnoreCase("SUBSYSTEM") && !_configuration.getEnableCUA()){
								continue;
							}
							List<Object> lsObj= atr.getValue();
							if(!(targetAttrName.equalsIgnoreCase("name") || targetAttrName.equalsIgnoreCase("selected") ||
									targetAttrName.equalsIgnoreCase("newAttr"))){
								if (key.equals("roles") || key.equals("profiles")) {
									if (j == 0) {
										function.appendTableRow(actgrpTable,
												targetAttrName,
												lsObj.get(0).toString().substring(lsObj.get(0).toString().lastIndexOf(ch) + 1));
										if(_configuration.getEnableCUA() && _configuration.getmasterSystem()!= null){
											function.setTableRowValue(actgrpTable,"SUBSYSTEM",
												lsObj.get(0).toString().substring(0, lsObj.get(0).toString().lastIndexOf(ch)));
										}	
										j++;
									} else {
										if (atr.getName().equals("TO_DAT")
												|| atr.getName().equals("FROM_DAT")) {
										if(lsObj != null && !lsObj.get(0).toString().equals("0")){		
												Date date = null;
												if (lsObj.get(0) instanceof String) {
													date = SAPUtil
													.stringToDate(lsObj.get(0).toString(),
															null);
													
												} else {
													 date = new Date((Long)lsObj.get(0));
												}
												/*Date date = SAPUtil
														.stringToDate(lsObj.get(0).toString(),
																"MM/dd/yyyy");*/
												function.setTableRowValue(actgrpTable,
														targetAttrName,
														date);
											}	
										} else {
											function.setTableRowValue(actgrpTable,
													targetAttrName.trim(),
													lsObj.get(0).toString().substring(lsObj.get(0).toString().lastIndexOf(ch) + 1));
										}
										
									}
								} else {
									if (j == 0) {
										function.appendTableRow(actgrpTable,
												targetAttrName.trim(),
												lsObj.get(0).toString().substring(lsObj.get(0).toString().lastIndexOf(ch) + 1));										    
										if(key.equals("parameters")){
										    	function.setImportValue("PARAMETERX", targetAttrName.trim(), lsObj.get(0).toString(), false, null);
										    } else {
										    	function.setImportValue(tableName+"X", targetAttrName.trim(), lsObj.get(0).toString(), false, null);
										    }	
										j++;
									} else {
										function.setTableRowValue(actgrpTable,
												targetAttrName.trim(),
												lsObj.get(0).toString().substring(lsObj.get(0).toString().lastIndexOf(ch) + 1));
										if(key.equals("parameters")){
									    	function.setImportValue("PARAMETERX", targetAttrName.trim(), lsObj.get(0).toString(), false, null);
									    } else {
									    	function.setImportValue(tableName+"X", targetAttrName.trim(), lsObj.get(0).toString(), false, null);
									    }	
										
									}
									
								} 
							} 
						}	
					}
				} else {
					if ((key.equalsIgnoreCase("groups") || key.equalsIgnoreCase("parameters")) && activityGroups == null){
						if(key.equals("parameters")){
					    	function.setImportValue("PARAMETERX", "PARID", "X", false, null);
					    } else {
					    	function.setImportValue(tableName+"X", "USERGROUP", "X", false, null);
					    }
					}
				}
				function.executeWithRetry(_configuration.getMaxBAPIRetries());
				function.jcoErrorCheck();
			} catch (JCoException e) {
				String message = _configuration.getMessage(
						"SAP_ERR_LOC_ACTGRP_MGMT", accountId);
				log.error(message);
				ConnectorException ce = new ConnectorException(message, e);
				throw ce;
			}
			log.info("RETURN");
	}
	
	/**
	 * Updates the mastersystem name to created user.
	 * This functionality is supported only in oim,method will be executed only from OIM,
	 * 
	 * @param accountId
	 * @param subSystems
	 * @throws JCoException
	 * @throws ConnectorException
	 */
	protected void addMasterSystem(String accountId,
			List<Object> masterSystem) throws JCoException, ConnectorException {
		log.info("BEGIN");
		if (true) {
			Function function = null;
			try {
				function = new Function("BAPI_USER_SYSTEM_ASSIGN", accountId,
						_connection, _configuration.getConnectorMessages(),
						true, _configuration
								.getRetryWaitTime());
				function.setImportValue("SYSTEMS", "SUBSYSTEM", masterSystem,
						false);
			} catch (JCoException e) {
				String message = null;
				int group = e.getGroup();
				if (group != JCoException.JCO_ERROR_FUNCTION_NOT_FOUND) {
					message = _configuration.getMessage("SAP_ERR_CUASYSTEMS",
							accountId);
				} else {
					message = _configuration
							.getMessage("SAP_ERR_NO_SUBSYS_BAPI");
				} // group != JCO_ERROR_FUNCTION_NOT_FOUND
				log.error(e, message);
				ConnectorException ce = new ConnectorException(message, e);
				throw ce;
			}
			function.executeWithRetry(_configuration.getMaxBAPIRetries());
			function.jcoErrorCheck();
		}
		log.info("RETURN");
	}
	
	/**
	 * Description:This method is used to sync password of a user
	 * 
	 * @param sUserID
	 *          User ID of the user to be modified. For example: John.Doe
	 * @param sOldPassword
	 *          Old password of the user
	 * @param sNewPassword
	 *          New password of the user
	 * 
	 * @return String
	 * 
	 * @throws ConnectorException
	 */
	protected void changePassword(String bapiName, String sUserID, GuardedString sOldPassword,
			GuardedString sNewPassword) throws ConnectorException {
		log.info("BEGIN");
		try {
			Function function = new Function(bapiName,
					_connection, _configuration.getConnectorMessages(),true, _configuration.getRetryWaitTime());
			function.setImportValue(null, "BNAME", sUserID, false);
			function.setImportValue(null, "PASSWORD", sOldPassword, false);
			function.setImportValue(null, "NEW_PASSWORD", sNewPassword, false);
		
			function.executeWithRetry(_configuration.getMaxBAPIRetries());
			function.jcoErrorCheck();
			// handle the case that we DO NOT have the SAP note 899614
			// this keeps backwards compatibility alive
		} catch (JCoException e) {
			log.error(e, "SAP_ERR_CHANGING_PASSWD");
			if (_configuration.getReturnSAPTemporaryPwdsOnFailure()) {
				String message = _configuration.getMessage(
						"SAP_ERR_DISPLAY_TMP_PWD", sNewPassword);
				log.error(message);
				throw new ConnectorException(message, e);
			}
			throw new ConnectorException(e);
		}
		log.info("RETURN");
	}
	
	/**
	 * Handle the custom attribute.
	 * 
	 * @param function
	 * @param attr
	 * @param key
	 * @throws JCoException
	 */
	protected void addCustomAttribute(Function function, Attribute attr, String sUserID) throws JCoException {
		log.info("BEGIN");
		String key = attr.getName();
		Object value = attr.getValue();
		String[] split = key.split(SAPConnector.DELIMITER);
		function = new Function(split[3],
				 _connection, _configuration
						.getConnectorMessages(), _configuration
						.getEnableCUA(), _configuration
						.getRetryWaitTime());
		
		function.setImportValue(null, split[4], sUserID, false);
		function.setImportValue(split[0], split[1], value, true);
		function.executeWithRetry( _configuration.getMaxBAPIRetries());
		function.jcoErrorCheck();
		log.info("RETURN");
	}
	
	
	/**
	 * This method handles the normal SAP activity group assignement as well as
	 * hands off subsystem activity group assignment.This method is only for Adding the multivalue attribute from OIM. 
	 * 
	 * @param activityGroups
	 * @param accountId
	 * @param key
	 * @param _connection
	 * @param _configuration
	 * @throws ConnectorException
	 */
	protected void addAttributeValues(List<Object> activityGroups ,
			String key, String accountId) throws ConnectorException {
		log.info("BEGIN");
		SimpleDateFormat dateFormatter = new SimpleDateFormat("MM/dd/yyyy");
		int ch = '~';		
		JCoTable jcoTable = null;
		String[] split = null;
			try {
				log.info("key: "+key);
				String bapiName = SAPUtil.getAddMultiValueDataBAPIName(key, _configuration.getEnableCUA());				
				log.info("bapiName: "+bapiName);
				String tableName = null;
				if(key.equalsIgnoreCase("roles")){
					tableName = _configuration.getRoles().substring(0,_configuration.getRoles().lastIndexOf(ch));
					jcoTable = getRoleorProfile(accountId, tableName, _connection, _configuration);
					split = _configuration.getRoles().split("~");
					split = split[1].split(";");
				} else if(key.equalsIgnoreCase("profiles")){
					tableName = _configuration.getProfiles().substring(0,_configuration.getProfiles().lastIndexOf(ch));					
					jcoTable = getRoleorProfile(accountId, tableName, _connection,_configuration);
					split = _configuration.getProfiles().split("~");
					split = split[1].split(";");
				} else if(key.equals("parameters")){
					tableName = _configuration.getParameters().substring(0,_configuration.getParameters().lastIndexOf(ch));					
					jcoTable = getRoleorProfile(accountId, tableName, _connection,_configuration);
					split = _configuration.getParameters().split("~");
					split = split[1].split(";");
				} else if(key.equals("groups")){
					tableName = _configuration.getGroups().substring(0,_configuration.getGroups().lastIndexOf(ch));					
					jcoTable = getRoleorProfile(accountId, tableName, _connection,_configuration);
					split = _configuration.getGroups().split("~");
					split = split[1].split(";");
				}
				for (int i = 0; i < split.length; i++) {
					log.info("List: " + split[i]);
				}
				log.info("tableName: "+tableName);
				log.info("CUA Mode:"+_configuration.getEnableCUA());
				Function function = new Function(
						bapiName, accountId.toUpperCase(),
						_connection, _configuration.getConnectorMessages(),
						_configuration.getEnableCUA(), _configuration
								.getRetryWaitTime());
				JCoTable actgrpTable = function.getTable(tableName);
				int j = 0;
				if(activityGroups != null){
					for(int i=0;i<activityGroups.size();i++){
						j = 0;
						EmbeddedObject activityGrpBuilder=(EmbeddedObject) activityGroups.get(i); 
						Set<Attribute> attrAct= activityGrpBuilder.getAttributes(); 
						Iterator<Attribute> iter = attrAct.iterator();
						while (iter.hasNext()) {
							Attribute atr=(Attribute) iter.next(); 
							String targetAttrName=atr.getName();
							log.info("targetAttrName:"+targetAttrName); 
							if(targetAttrName.equalsIgnoreCase("PROFILE") && !_configuration.getEnableCUA()){
								targetAttrName = "BAPIPROF";
							}
							if(targetAttrName.equalsIgnoreCase("SUBSYSTEM") && !_configuration.getEnableCUA()){
								continue;
							}
							List<Object> lsObj= atr.getValue();
							log.info("lsObj Value :"+lsObj);
							if(!(targetAttrName.equalsIgnoreCase("name") || targetAttrName.equalsIgnoreCase("selected") ||
									targetAttrName.equalsIgnoreCase("newAttr"))){
								/*if (key.equals("roles") || key.equals("profiles")) {
									if (j == 0) {
										function.appendTableRow(actgrpTable,
												targetAttrName,
												lsObj.get(0).toString().substring(lsObj.get(0).toString().lastIndexOf(ch) + 1));
										if(_configuration.getEnableCUA() && _configuration.getmasterSystem()!= null){
											function.setTableRowValue(actgrpTable,"SUBSYSTEM",
												lsObj.get(0).toString().substring(0, lsObj.get(0).toString().lastIndexOf(ch)));
										}	
										j++;*/							
								if (key.equals("roles") || key.equals("profiles")) {
									if (j == 0) {
										if(atr.getName().equals("TO_DAT")
												|| atr.getName().equals("FROM_DAT")) {
											if(!lsObj.get(0).toString().equals("0")){												
												Date date = null;
												if (lsObj.get(0) instanceof String) {
													date = SAPUtil
													.stringToDate(lsObj.get(0).toString(),
															null);													
												} else {
													 date = new Date((Long)lsObj.get(0));
												}		
												function.appendTableRow(actgrpTable,
														targetAttrName,
														date);
												log.info(targetAttrName+"append success with value:"+date);
											}else{	
												log.info("appending empty row since" +targetAttrName+ "is :" +lsObj);
												actgrpTable.appendRow();
												log.info("Successfully appended empty row");
											}
										} else if(atr.getName().equals("AGR_NAME") || atr.getName().equals("PROFILE") || atr.getName().equals("BAPIPROF")) {																						
											function.appendTableRow(actgrpTable,
													targetAttrName,
													lsObj.get(0).toString().substring(lsObj.get(0).toString().lastIndexOf(ch) + 1));
											
											if (_configuration.getEnableCUA() && _configuration.getmasterSystem()!= null){
												function.setTableRowValue(actgrpTable,"SUBSYSTEM",
													lsObj.get(0).toString().substring(0, lsObj.get(0).toString().lastIndexOf(ch)));
											}
										}
										// START Bug 27280110 - AOB: REG SAP UM WITH CUA ROLES ARE NOT ASSIGN TO THE USER
										// Since Subsystem is handled as part of AGR_NAME. Excluding Subsystem
										if(! targetAttrName.equalsIgnoreCase("SUBSYSTEM")){
											j++;
										}
										//END Bug 27280110 - AOB: REG SAP UM WITH CUA ROLES ARE NOT ASSIGN TO THE USER
									} else {
										if (atr.getName().equals("TO_DAT")
												|| atr.getName().equals("FROM_DAT")) {											
											if(!lsObj.get(0).toString().equals("0")){												
												Date date = null;
												if (lsObj.get(0) instanceof String) {																										
													date = SAPUtil
													.stringToDate(lsObj.get(0).toString(),
															null);
													log.info("date: "+date);
												} else {													
													 date = new Date((Long)lsObj.get(0));
													 log.info("date: "+date);
												}														
												function.setTableRowValue(actgrpTable,
														targetAttrName,
														date);
											}	
										/*} else {
											function.setTableRowValue(actgrpTable,
													targetAttrName.trim(),
													lsObj.get(0).toString().substring(lsObj.get(0).toString().lastIndexOf(ch) + 1));
										}*/
									} else if(atr.getName().equals("AGR_NAME") || atr.getName().equals("PROFILE") || atr.getName().equals("BAPIPROF")) {																				
										function.setTableRowValue(actgrpTable,
												targetAttrName.trim(),
												lsObj.get(0).toString().substring(lsObj.get(0).toString().lastIndexOf(ch) + 1));
										if(_configuration.getEnableCUA() && _configuration.getmasterSystem()!= null){
											function.setTableRowValue(actgrpTable,"SUBSYSTEM",
												lsObj.get(0).toString().substring(0, lsObj.get(0).toString().lastIndexOf(ch)));
										}
									}										
									}
								} else {
									if (j == 0) {
										function.appendTableRow(actgrpTable,
												targetAttrName.trim(),
												lsObj.get(0).toString().substring(lsObj.get(0).toString().lastIndexOf(ch) + 1));										    
										if(key.equals("parameters")){
										    	function.setImportValue("PARAMETERX", targetAttrName.trim(), lsObj.get(0).toString(), false, null);
										    } else {
										    	function.setImportValue(tableName+"X", targetAttrName.trim(), lsObj.get(0).toString(), false, null);
										    }	
										j++;
									} else {
										function.setTableRowValue(actgrpTable,
												targetAttrName.trim(),
												lsObj.get(0).toString().substring(lsObj.get(0).toString().lastIndexOf(ch) + 1));																				
										if(key.equals("parameters")){
									    	function.setImportValue("PARAMETERX", targetAttrName.trim(), lsObj.get(0).toString(), false, null);
									    } else {
									    	function.setImportValue(tableName+"X", targetAttrName.trim(), lsObj.get(0).toString(), false, null);
									    }											
									}									
								} 
							} 
						}	
					}
				} else {
					if ((key.equalsIgnoreCase("groups") || key.equalsIgnoreCase("parameters")) && activityGroups == null){						
						if(key.equals("parameters")){
					    	function.setImportValue("PARAMETERX", "PARID", "X", false, null);
					    } else {
					    	function.setImportValue(tableName+"X", "USERGROUP", "X", false, null);
					    }
					}
				}
				
				int iTableFieldRowCount = jcoTable.getNumRows();
				for (int iIndex = 0; iIndex < iTableFieldRowCount; iIndex++) {
				jcoTable.setRow(iIndex);
				j=0;
				for (int jIndex = 0; jIndex < split.length; jIndex++) {
					if (key.equals("roles") || key.equals("profiles")) {
						if (j == 0) {
							if(!_configuration.getEnableCUA() && split[jIndex].equalsIgnoreCase("SUBSYSTEM"))
								continue;
								
							if(split[jIndex].equalsIgnoreCase("PROFILE") && !_configuration.getEnableCUA()){
								split[jIndex] = "BAPIPROF";
							}	
							//Bug 20260847 - SAP UM UNPARSEABLE DATE ERROR DUE TO MISMATCH IN LOCALE
							if(split[jIndex].equals("TO_DAT") || split[jIndex].equals("FROM_DAT")){
								function.appendTableRow(actgrpTable, split[jIndex],
										(Date)jcoTable.getValue(split[jIndex]));
							}else{
							function.appendTableRow(actgrpTable, split[jIndex],
									(jcoTable.getValue(split[jIndex]))
											.toString());
							}
							//Bug 20260847 - SAP UM UNPARSEABLE DATE ERROR DUE TO MISMATCH IN LOCALE
							j++;
							//Bug 20260847 - SAP UM UNPARSEABLE DATE ERROR DUE TO MISMATCH IN LOCALE
						} else {
							if(split[jIndex].equals("TO_DAT") || split[jIndex].equals("FROM_DAT")){
                                if(jcoTable.getValue(split[jIndex]) instanceof String){
                                    function.setTableRowValue(actgrpTable,
                                            split[jIndex],SAPUtil.stringToDate(jcoTable
                                                    .getValue(split[jIndex]).toString(),"EEE MMM dd HH:mm:ss z yyyy"));
                                }else{
                                	
                                    function.setTableRowValue(actgrpTable,
                                            split[jIndex],(Date)jcoTable.getValue(split[jIndex]));
                                }

						}else {
									function.setTableRowValue(actgrpTable,
											split[jIndex], (jcoTable
											.getValue(split[jIndex]))
											.toString());
							//Bug 20260847 - SAP UM UNPARSEABLE DATE ERROR DUE TO MISMATCH IN LOCALE
							}
						} 
					}
					else {					
						if (j == 0) {
							function.appendTableRow(actgrpTable, split[jIndex],
									(jcoTable.getValue(split[jIndex]))
											.toString());
							if (key.equals("parameters")) {
								function.setImportValue("PARAMETERX",
										split[jIndex], "X", false, null);
							} else {
								function.setImportValue(tableName + "X",
										split[jIndex], "X", false, null);
							}
							j++;
						} else {
							function.setTableRowValue(actgrpTable,
									split[jIndex], (jcoTable
											.getValue(split[jIndex]))
											.toString());
							if (key.equals("parameters")) {
								function.setImportValue("PARAMETERX",
										split[jIndex], "X", false, null);
							} else {
								function.setImportValue(tableName + "X",
										split[jIndex], "X", false, null);
							}
						}
					}
				}
			}
					
				function.executeWithRetry(_configuration.getMaxBAPIRetries());
				function.jcoErrorCheck();
			} catch (Exception e) {
				String message = _configuration.getMessage(
						"SAP_ERR_LOC_ACTGRP_MGMT", accountId);
				log.error(message);
				ConnectorException ce = new ConnectorException(message, e);
				throw ce;
			}
			log.info("RETURN");
	}
	
	/**
	 * This method handles the normal SAP activity group removal as well as
	 * hands off subsystem activity group removal.This method is only to Remove the multivalue attribute sent from OIM.
	 * 
	 * @param activityGroups
	 * @param accountId
	 * @param key
	 * @param _connection
	 * @param _configuration
	 * @throws ConnectorException
	 * 
	 */
	protected void removeAttributeValues(List<Object> activityGroups,
			String key, String accountId) throws ConnectorException {
		log.info("BEGIN");
		int ch = '~';
		JCoTable jcoTable = null;
		String[] split = null;
		try {
			log.info("key: "+key);
			String bapiName = SAPUtil.getAddMultiValueDataBAPIName(key,
					_configuration.getEnableCUA());
			log.info("bapiName: "+bapiName);
			String tableName = null;
			if (key.equalsIgnoreCase("roles")) {
				tableName = _configuration.getRoles().substring(0,
						_configuration.getRoles().lastIndexOf(ch));
				jcoTable = getRoleorProfile(accountId, tableName, _connection,_configuration);
				split = _configuration.getRoles().split("~");
				split = split[1].split(";");
			} else if (key.equalsIgnoreCase("profiles")) {
				tableName = _configuration.getProfiles().substring(0,
						_configuration.getProfiles().lastIndexOf(ch));
				jcoTable = getRoleorProfile(accountId, tableName, _connection,_configuration);
				split = _configuration.getProfiles().split("~");
				split = split[1].split(";");
			} else if (key.equals("parameters")) {
				tableName = _configuration.getParameters().substring(0,
						_configuration.getParameters().lastIndexOf(ch));
				jcoTable = getRoleorProfile(accountId, tableName, _connection,_configuration);
				split = _configuration.getParameters().split("~");
				split = split[1].split(";");
			} else if (key.equals("groups")) {
				tableName = _configuration.getGroups().substring(0,
						_configuration.getGroups().lastIndexOf(ch));
				jcoTable = getRoleorProfile(accountId, tableName, _connection,_configuration);
				split = _configuration.getGroups().split("~");
				split = split[1].split(";");
			}
			for (int i = 0; i < split.length; i++) {
				log.info("List: " + split[i]);
			}
			log.info("tableName: "+tableName);
			log.info("CUA Mode:"+_configuration.getEnableCUA());
			Function function = new Function(bapiName, accountId.toUpperCase(),
					_connection, _configuration.getConnectorMessages(),
					_configuration.getEnableCUA(), _configuration
							.getRetryWaitTime());
			JCoTable actgrpTable = function.getTable(tableName);
			int iTableFieldRowCount = jcoTable.getNumRows();
			boolean isDelete = false;
			int j = 0;
			if (activityGroups != null) {
				for (int iIndex = 0; iIndex < iTableFieldRowCount; iIndex++) {
					if (isDelete) {
						jcoTable.deleteRow();
					}
					jcoTable.setRow(iIndex);
				for (int i = 0; i < activityGroups.size(); i++) {
					j = 0;
					EmbeddedObject activityGrpBuilder = (EmbeddedObject) activityGroups
							.get(i);
					Set<Attribute> attrAct = activityGrpBuilder.getAttributes();
					Iterator<Attribute> iter = attrAct.iterator();
						while (iter.hasNext()) {
							Attribute atr = (Attribute) iter.next();
							String targetAttrName = atr.getName();
							log.info("targetAttrName:"+targetAttrName);
							if (targetAttrName.equalsIgnoreCase("PROFILE")
									&& !_configuration.getEnableCUA()) {
								targetAttrName = "BAPIPROF";
							}
							if (targetAttrName.equalsIgnoreCase("SUBSYSTEM")
									&& !_configuration.getEnableCUA()) {
								continue;
							}
							List<Object> lsObj = atr.getValue();
							log.info("lsObj Value :"+lsObj);
							if (!(targetAttrName.equalsIgnoreCase("name")
									|| targetAttrName
											.equalsIgnoreCase("selected") || targetAttrName
									.equalsIgnoreCase("newAttr"))) {
								if (key.equals("roles")
										|| key.equals("profiles")) {																										
									 if (j == 0) {										 
										 	if ((atr.getName().equals("TO_DAT")
											|| atr.getName().equals(
											"FROM_DAT"))) {										 		
												if (lsObj != null && lsObj
														.get(0) != null && !lsObj.get(0).toString()
														.equals("0")) {													
													Date targetDate = null;
													Date formDate = null;
													//Bug 20260847 - SAP UM UNPARSEABLE DATE ERROR DUE TO MISMATCH IN LOCALE
													if(jcoTable.getValue(targetAttrName.trim()) instanceof String){
														targetDate = SAPUtil.stringToDate(jcoTable.getValue(targetAttrName.trim()).toString(),"EEE MMM dd HH:mm:ss z yyyy");
														}
														else{
															targetDate=(Date)jcoTable.getValue(targetAttrName.trim());
														}
													//Bug 20260847 - SAP UM UNPARSEABLE DATE ERROR DUE TO MISMATCH IN LOCALE
													log.info("targetDate:"+targetDate);
													if (lsObj != null && lsObj.get(0) !=null && lsObj.get(0) instanceof String) {
														formDate = SAPUtil
																.stringToDate(
																		lsObj
																				.get(
																						0)
																				.toString(),
																		null);
														log.info("formDate1:"+formDate);
													} else {
														if(lsObj != null && lsObj.get(0) != null)
															formDate = new Date(
																(Long) lsObj.get(0));
														log.info("formDate2:"+formDate);
													}
													if (targetDate
															.compareTo(formDate) == 0) {
														isDelete = true;													
													} else {
														isDelete = false;
														continue;
													}
												}
											}else if (lsObj != null && lsObj
												.get(0)
												.toString()
												.substring(
														lsObj
																.get(0)
																.toString()
																.lastIndexOf(ch) + 1)
												.equalsIgnoreCase(
														jcoTable
																.getValue(
																		targetAttrName
																				.trim())
																.toString())) {											
											isDelete = true;											
											if (_configuration.getEnableCUA()
													&& _configuration
															.getmasterSystem() != null) {
												if (lsObj != null && lsObj.get(0).toString().substring(0, lsObj.get(0).toString().lastIndexOf(ch))
														.equalsIgnoreCase(
																jcoTable
																		.getValue(
																				"SUBSYSTEM")
																		.toString())) {													
													isDelete = true;													
												} else {
													isDelete = false;												
													continue;
												}
											}
											j++;
										} else {
											isDelete = false;
											j++;
											continue;
										}										
									} else {										
										if (atr.getName().equals("TO_DAT")
												|| atr.getName().equals(
														"FROM_DAT")) {											
											if (lsObj != null && lsObj
													.get(0) != null && !lsObj.get(0).toString()
													.equals("0")) {
												Date targetDate = null;
												Date formDate = null;
												//Bug 20260847 - SAP UM UNPARSEABLE DATE ERROR DUE TO MISMATCH IN LOCALE
												if(jcoTable.getValue(targetAttrName.trim()) instanceof String){
													targetDate = SAPUtil.stringToDate(jcoTable.getValue(targetAttrName.trim()).toString(),"EEE MMM dd HH:mm:ss z yyyy");
													}
													else{
														targetDate=(Date)jcoTable.getValue(targetAttrName.trim());
													}
												//Bug 20260847 - SAP UM UNPARSEABLE DATE ERROR DUE TO MISMATCH IN LOCALE
												log.info("targetDate:"+targetDate);
												if (lsObj != null && lsObj.get(0) !=null && lsObj.get(0) instanceof String) {
													formDate = SAPUtil
															.stringToDate(
																	lsObj
																			.get(
																					0)
																			.toString(),
																	null);
													log.info("formDate3:"+formDate);
												} else {
													if(lsObj != null && lsObj.get(0) != null)
														formDate = new Date(
															(Long) lsObj.get(0));
													log.info("formDate4:"+formDate);
												}
												if (targetDate
														.compareTo(formDate) == 0) {
													isDelete = true;
												} else {
													isDelete = false;
													continue;
												}
											}
										} else {
											if (lsObj != null && lsObj
													.get(0)
													.toString()
													.substring(
															lsObj
																	.get(0)
																	.toString()
																	.lastIndexOf(
																			ch) + 1)
													.equalsIgnoreCase(
															jcoTable
																	.getValue(
																			targetAttrName
																					.trim())
																	.toString())) {
												isDelete = true;
												if (_configuration.getEnableCUA()
														&& _configuration
																.getmasterSystem() != null) {
													if (lsObj != null && lsObj.get(0).toString().substring(0, lsObj.get(0).toString().lastIndexOf(ch))
															.equalsIgnoreCase(
																	jcoTable
																			.getValue(
																					"SUBSYSTEM")
																			.toString())) {														
														isDelete = true;														
													} else {
														isDelete = false;													
														continue;
													}
												}												
											} else {
												isDelete = false;
												continue;
											}																						
										}

									}
								} else {
									if (j == 0) {
										if (lsObj != null && lsObj
												.get(0)
												.toString()
												.substring(
														lsObj
																.get(0)
																.toString()
																.lastIndexOf(ch) + 1)
												.equalsIgnoreCase(
														jcoTable
																.getValue(
																		targetAttrName
																				.trim())
																.toString())) {
											isDelete = true;
											j++;
										} else {
											isDelete = false;
											j++;
											continue;
										}
									} else {										
										if (lsObj != null && lsObj
												.get(0)
												.toString()
												.substring(
														lsObj
																.get(0)
																.toString()
																.lastIndexOf(ch) + 1)
												.equalsIgnoreCase(
														jcoTable
																.getValue(
																		targetAttrName
																				.trim())
																.toString())) {
											isDelete = true;
										} else {
											isDelete = false;
											continue;
										}
									}

								}
							}
						}
					}
				}
				if (isDelete) {
					jcoTable.deleteRow();
					log.info("Row Deleted");
				}
			}

			iTableFieldRowCount = jcoTable.getNumRows();
			for (int iIndex = 0; iIndex < iTableFieldRowCount; iIndex++) {
				jcoTable.setRow(iIndex);
				j = 0;
				for (int jIndex = 0; jIndex < split.length; jIndex++) {
					if (key.equals("roles") || key.equals("profiles")) {
						if (j == 0) {
							if (!_configuration.getEnableCUA()
									&& split[jIndex]
											.equalsIgnoreCase("SUBSYSTEM"))
								continue;

							if (split[jIndex].equalsIgnoreCase("PROFILE")
									&& !_configuration.getEnableCUA()) {
								split[jIndex] = "BAPIPROF";
							}
							//Bug 20260847 - SAP UM UNPARSEABLE DATE ERROR DUE TO MISMATCH IN LOCALE
							if(split[jIndex].equals("TO_DAT") || split[jIndex].equals("FROM_DAT")){
								function.appendTableRow(actgrpTable, split[jIndex],
										(Date)jcoTable.getValue(split[jIndex]));
							}else{
							function.appendTableRow(actgrpTable, split[jIndex],
									(jcoTable.getValue(split[jIndex]))
											.toString());
							}
							//Bug 20260847 - SAP UM UNPARSEABLE DATE ERROR DUE TO MISMATCH IN LOCALE
							j++;
							//Bug 20260847 - SAP UM UNPARSEABLE DATE ERROR DUE TO MISMATCH IN LOCALE
						} else {
							if(split[jIndex].equals("TO_DAT") || split[jIndex].equals("FROM_DAT")){
                                if(jcoTable.getValue(split[jIndex]) instanceof String){
                                    function.setTableRowValue(actgrpTable,
                                            split[jIndex],SAPUtil.stringToDate(jcoTable
                                                    .getValue(split[jIndex]).toString(),"EEE MMM dd HH:mm:ss z yyyy"));
                                }else{
                                	
                                    function.setTableRowValue(actgrpTable,
                                            split[jIndex],(Date)jcoTable.getValue(split[jIndex]));
                                }

						}else {
									function.setTableRowValue(actgrpTable,
											split[jIndex], (jcoTable
											.getValue(split[jIndex]))
											.toString());
							//Bug 20260847 - SAP UM UNPARSEABLE DATE ERROR DUE TO MISMATCH IN LOCALE
							  }
							}
						
					} else {
						///j = 0;
						if (j == 0) {
							function.appendTableRow(actgrpTable, split[jIndex],
									(jcoTable.getValue(split[jIndex]))
											.toString());
							if (key.equals("parameters")) {
								function.setImportValue("PARAMETERX",
										split[jIndex], "X", false, null);
							} else {
								function.setImportValue(tableName + "X",
										split[jIndex], "X", false, null);
							}
							j++;
						} else {
							function.setTableRowValue(actgrpTable,
									split[jIndex], (jcoTable
											.getValue(split[jIndex]))
											.toString());
							if (key.equals("parameters")) {
								function.setImportValue("PARAMETERX",
										split[jIndex], "X", false, null);
							} else {
								function.setImportValue(tableName + "X",
										split[jIndex], "X", false, null);
							}
						}
					}
				}
			}
			
			if(iTableFieldRowCount == 0){
				if (key.equalsIgnoreCase("groups") || key.equalsIgnoreCase("parameters")){
					if(key.equals("parameters")){
				    	function.setImportValue("PARAMETERX", "PARID", "X", false, null);
				    } else {
				    	function.setImportValue(tableName+"X", "USERGROUP", "X", false, null);
				    }
				}
			}

			function.executeWithRetry(_configuration.getMaxBAPIRetries());
			function.jcoErrorCheck();
		} catch (JCoException e) {
			String message = _configuration.getMessage(
					"SAP_ERR_LOC_ACTGRP_MGMT", accountId);
			log.error(message);
			ConnectorException ce = new ConnectorException(message, e);
			throw ce;
		}
		log.info("RETURN");
	}
		// Start:: Bug 19567995 - WHEN AN USER FROM CUA IS DISABLED AND ENABLED AGAIN, THE USER IS STILL DISABLED
	private  String getUserLockStatus(String accountId){
		String lockStatus=null;
		log.info("BEGIN");
		final JCoTable jcoTable;
		final StringBuffer whereClause;
        
		try {
			Function function = new Function("RFC_READ_TABLE",_connection, _configuration
							.getConnectorMessages(), _configuration
							.getEnableCUA(), _configuration.getRetryWaitTime());
			log.info("Execute RFC_READ_TABLE ({0})", accountId);
			// function.execute();
			JCoParameterList input = function.getImportParameterList();
			input.setValue("QUERY_TABLE","USR02");			
				jcoTable = function.getTableParameterList().getTable("OPTIONS");
				jcoTable.appendRow();
		        whereClause = new StringBuffer(25);
		        whereClause.append("BNAME EQ '");
		        whereClause.append(accountId);
		        whereClause.append("'");
		        jcoTable.setValue("TEXT", whereClause.toString());
		        JCoTable jcoTable1 = function.getTableParameterList().getTable("FIELDS");
		        jcoTable1.appendRow();
		        jcoTable1.setValue("FIELDNAME","UFLAG");
		        function.execute();
			    JCoParameterList tableParameterList = function.getTableParameterList();
		        JCoTable datum = tableParameterList.getTable("DATA");
		        final int numRows;
		        numRows = datum.getNumRows();
		        if (numRows == 1){
			    	   lockStatus = datum.getString("WA");
			       }			
		} catch (JCoException ex) {
			log.error(ex.getMessage());
			ex.printStackTrace();
		}
		catch (Exception ex) {
			log.error(ex.getMessage());
			ex.printStackTrace();
		}	
		log.info("RETURN");
		return lockStatus;
	}
	// End :: Bug 19567995 - WHEN AN USER FROM CUA IS DISABLED AND ENABLED AGAIN, THE USER IS STILL DISABLED
}
